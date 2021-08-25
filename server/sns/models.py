from django.db import models

# Create your models here.
class User(models.Model):
    userID = models.BigAutoField(primary_key=True)
    deviceID = models.CharField(max_length=100, unique=True)
    username = models.CharField(max_length=100, unique=True)
    profileImage = models.ImageField(upload_to='profiles')

class Post(models.Model):
    postID = models.BigAutoField(primary_key=True)
    content = models.CharField(max_length=500)
    author = models.ForeignKey(User, related_name="author", on_delete=models.CASCADE, db_column='author_id')
    pos_latitude = models.FloatField()
    pos_longitude = models.FloatField()
    uploadTime = models.DateTimeField(auto_now_add=True)

class PostImage(models.Model):
    post = models.ForeignKey(Post, related_name="postImage", on_delete=models.CASCADE, db_column='postimage_id')
    post_image = models.ImageField(upload_to='posts')

class Comment(models.Model):
    author = models.ForeignKey(User, related_name="comment", on_delete=models.CASCADE, db_column='author_comment_id')
    post = models.ForeignKey(Post, related_name="comment", on_delete=models.CASCADE, db_column="post_comment_id")
    content = models.CharField(max_length=100)

class Like(models.Model):
    post = models.ForeignKey(Post, related_name="like", on_delete=models.CASCADE, db_column="liked_id")
    liker = models.IntegerField()
