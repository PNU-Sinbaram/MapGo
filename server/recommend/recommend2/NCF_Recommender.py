import datetime
import os
import pandas as pd
import numpy as np
import tensorflow as tf
import tensorflow.keras as keras

from tensorflow.keras.layers import (
    Concatenate,
    Dense,
    Embedding,
    Flatten,
    Input,
    Multiply,
)
from tensorflow.keras.models import Model
from tensorflow.keras.regularizers import l2
from tensorflow.keras.optimizers import Adam

from datetime import date

from typing import List


def create_ncf(
    number_of_users: int,
    number_of_items: int,
    latent_dim_mf: int = 4,
    latent_dim_mlp: int = 32,
    reg_mf: int = 0,
    reg_mlp: int = 0.01,
    dense_layers: List[int] = [8, 4],
    reg_layers: List[int] = [0.01, 0.01],
    activation_dense: str = "relu",
) -> keras.Model:

    # input layer
    user = Input(shape=(), dtype="int32", name="user_id")
    item = Input(shape=(), dtype="int32", name="item_id")

    # embedding layers
    mf_user_embedding = Embedding(
        input_dim=number_of_users,
        output_dim=latent_dim_mf,
        name="mf_user_embedding",
        embeddings_initializer="RandomNormal",
        embeddings_regularizer=l2(reg_mf),
        input_length=1,
    )
    mf_item_embedding = Embedding(
        input_dim=number_of_items,
        output_dim=latent_dim_mf,
        name="mf_item_embedding",
        embeddings_initializer="RandomNormal",
        embeddings_regularizer=l2(reg_mf),
        input_length=1,
    )

    mlp_user_embedding = Embedding(
        input_dim=number_of_users,
        output_dim=latent_dim_mlp,
        name="mlp_user_embedding",
        embeddings_initializer="RandomNormal",
        embeddings_regularizer=l2(reg_mlp),
        input_length=1,
    )
    mlp_item_embedding = Embedding(
        input_dim=number_of_items,
        output_dim=latent_dim_mlp,
        name="mlp_item_embedding",
        embeddings_initializer="RandomNormal",
        embeddings_regularizer=l2(reg_mlp),
        input_length=1,
    )

    # MF vector
    mf_user_latent = Flatten()(mf_user_embedding(user))
    mf_item_latent = Flatten()(mf_item_embedding(item))
    mf_cat_latent = Multiply()([mf_user_latent, mf_item_latent])

    # MLP vector
    mlp_user_latent = Flatten()(mlp_user_embedding(user))
    mlp_item_latent = Flatten()(mlp_item_embedding(item))
    mlp_cat_latent = Concatenate()([mlp_user_latent, mlp_item_latent])

    mlp_vector = mlp_cat_latent

    # build dense layers for model
    for i in range(len(dense_layers)):
        layer = Dense(
            dense_layers[i],
            activity_regularizer=l2(reg_layers[i]),
            activation=activation_dense,
            name="layer%d" % i,
        )
        mlp_vector = layer(mlp_vector)

    predict_layer = Concatenate()([mf_cat_latent, mlp_vector])

    result = Dense(
        1, activation="sigmoid", kernel_initializer="lecun_uniform", name="interaction"
    )

    output = result(predict_layer)

    model = Model(
        inputs=[user, item],
        outputs=[output],
    )

    return model

def make_tf_dataset(
    df: pd.DataFrame,
    targets: List[str],
    val_split: float = 0.1,
    batch_size: int = 8192,
    seed=42,
):
    n_val = round(df.shape[0] * val_split)
    if seed:
        # shuffle all the rows
        x = df.sample(frac=1, random_state=seed).to_dict("series")
    else:
        x = df.to_dict("series")
    y = dict()
    for t in targets:
        y[t] = x.pop(t)
    ds = tf.data.Dataset.from_tensor_slices((x, y))

    ds_val = ds.take(n_val).batch(batch_size)
    ds_train = ds.skip(n_val).batch(batch_size)
    return ds_train, ds_val

def eda():
    df = pd.read_csv('./checkin_history')
    df.drop('Unnamed: 0', axis=1)

    Checkins_table = pd.DataFrame({'reviewer': df['reviewer'], 'place': df['place']})
    Checkins_table = Checkins_table.place.groupby([Checkins_table.reviewer, Checkins_table.place]).size().unstack().fillna(0).astype(int)
    Checkins_table = (Checkins_table>0).astype(int)
    Checkins_table_rows = Checkins_table.index.values.tolist()
    Checkins_table_columns = Checkins_table.columns.tolist()
    unique_Checkins = np.unique(Checkins_table)
    Checkins_table = Checkins_table.to_numpy()

    return Checkins_table, unique_Checkins, Checkins_table_columns, Checkins_table_rows
 
def wide_to_long(wide: np.array, possible_ratings: List[int]) -> np.array:
    def _get_ratings(arr: np.array, rating: int) -> np.array:
        idx = np.where(arr == rating)
        return np.vstack(
            (idx[0], idx[1], np.ones(idx[0].size, dtype="int8") * rating)
        ).T

    long_arrays = []
    for r in possible_ratings:
        long_arrays.append(_get_ratings(wide, r))

    return np.vstack(long_arrays)

def main():
    Checkins_table, Unique_Checkins, Checkins_table_columns, Checkins_table_rows = eda()

    long_train = wide_to_long(Checkins_table, Unique_Checkins)
    df_train = pd.DataFrame(long_train, columns=["user_id", "item_id", "interaction"])
    df_test = df_train

    n_users, n_items = Checkins_table.shape
    ncf_model = create_ncf(n_users, n_items)

    ncf_model.compile(
        optimizer=Adam(),
        loss="binary_crossentropy",
        metrics=[
            tf.keras.metrics.TruePositives(name="tp"),
            tf.keras.metrics.FalsePositives(name="fp"),
            tf.keras.metrics.TrueNegatives(name="tn"),
            tf.keras.metrics.FalseNegatives(name="fn"),
            tf.keras.metrics.BinaryAccuracy(name="accuracy"),
            tf.keras.metrics.Precision(name="precision"),
            tf.keras.metrics.Recall(name="recall"),
            tf.keras.metrics.AUC(name="auc"),
        ],
    )
    ncf_model._name = "neural_collaborative_filtering"
    ncf_model.summary()

    ds_train, ds_val = make_tf_dataset(df_train, ["interaction"])
    ds_test, _ = make_tf_dataset(df_test, ["interaction"], val_split=0, seed=None)

    logdir = os.path.join("logs", datetime.datetime.now().strftime("%Y%m%d-%H%M%S"))
    tensorboard_callback = tf.keras.callbacks.TensorBoard(logdir, histogram_freq=1)

    train_hist = ncf_model.fit(
        ds_train,
        validation_data=ds_val,
        epochs=1,
        callbacks=[tensorboard_callback],
        verbose=1,
    )

    ncf_predictions = ncf_model.predict(ds_test)
    df_test["predictions"] = ncf_predictions

    data = df_test.pivot(
        index="user_id", columns="item_id", values="predictions"
    )
    data.columns = Checkins_table_columns
    data.index = Checkins_table_rows
    data.to_csv('./result.csv')

if __name__ == '__main__':
    main()
