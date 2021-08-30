from django.db import models


# Create your models here.
class User(models.Model):
    id = models.BigAutoField(primary_key=True)
    deviceID = models.CharField(max_length=100, unique=True)
    username = models.CharField(max_length=100, unique=True)
    picture = models.ImageField(upload_to='profiles')


class Post(models.Model):
    postID = models.BigAutoField(primary_key=True)
    contents = models.CharField(max_length=500)
    writer = models.ForeignKey(User, related_name="writer",
                               on_delete=models.CASCADE,
                               db_column='author_id')
    location = models.JSONField()
    postTime = models.DateTimeField(auto_now_add=True)


class PostImage(models.Model):
    post = models.ForeignKey(Post, related_name="postImage",
                             on_delete=models.CASCADE,
                             db_column='postimage_id')
    image = models.ImageField(upload_to='posts')


class Comment(models.Model):
    writer = models.ForeignKey(User, related_name="comment",
                               on_delete=models.CASCADE,
                               db_column='author_comment_id')
    post = models.ForeignKey(Post, related_name="comment",
                             on_delete=models.CASCADE,
                             db_column="post_comment_id")
    contents = models.CharField(max_length=100)


class Like(models.Model):
    post = models.ForeignKey(Post, related_name="like",
                             on_delete=models.CASCADE,
                             db_column="liked_id")
    liker = models.ForeignKey(User, related_name="like",
                              on_delete=models.CASCADE,
                              db_column='liker_id')
