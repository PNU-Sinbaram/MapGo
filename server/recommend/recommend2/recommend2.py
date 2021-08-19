import requests
import json
import os
import pandas as pd
from datetime import date


class recommend2: 
    def __init__(self):
        """Will be implemented later."""
        pass

    @staticmethod
    def recommend(UserID, lat, long, epsilon):
        """Returns list of place recommendation using NCF.

        Gets nearby place list from argument latitude, longitude using Google Place API,
        compares UserID, place list with NCF model prediction table output.

        Sorts comparison result as prediction probability, divide into percentage suggested by epsilon.
        Picks randomly 5 place list and returns as 2-dimension list.

        Args:
            UserID (string) : User ID(or device id) which will be the target of recommendation
            lat (float) : Latitude of current 'UserID' position
            long (float) : Longitude of current 'UserID' position
            epsilon (integer) : Percentage of recommedation scope

        Returns:
            2-D List : List of recommended place. Each element has place name, latitude, longitude, filtering
        """

        df = pd.read_csv('./recommendTable_'+str(date.today())+'.csv') 
        df = df.set_index('Unnamed: 0')

        locationList = []
        URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
        key = os.environ['GOOGLE_PLACES_KEY']
        location = str(lat)+','+str(long)
        params = {'key': key, 'location': location, 'radius': 100, 'language': 'ko'}
        resp = requests.get(URL, params=params)

        jsonObject = json.loads(resp.text)
        responseResult = jsonObject.get('results')
        for loc in responseResult:
            locationList.append(str(loc["vicinity"])+" "+str(loc["name"]))

        duplicatedColumn = set(df.columns.values.tolist()) & set(locationList)
        recommendResult = df[duplicatedColumn].loc[UserID].sort_values(axis=0, ascending=False)
        partialIndex = round(recommendResult.count()*(epsilon/100))
        if partialIndex < 5:
            partialIndex = 5
        recommendResult = recommendResult[:partialIndex]
        recommendResult = recommendResult.sample(n=5).sort_values(axis=0, ascending=False)
        print(recommendResult)
        return([["test3", 11.0, 11.0], ["test4", 22.0, 22.0]])

if __name__ == '__main__':
    recommend2.recommend("target", 35.231480, 129.085178, 100)
