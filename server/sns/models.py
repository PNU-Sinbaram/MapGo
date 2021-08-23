from django.db import models

# Create your models here.
class User(models.Model):
    userID = models.BigAutoField(primary_key=True)
    deviceID = models.CharField(max_length=100, unique=True)
    username = models.CharField(max_length=100, unique=True)
    profileImage = models.ImageField(upload_to='profiles')

class Post(models.Model):
    postID = models.BigAutoField(primary_key=True)
    title = models.CharField(max_length=100)
    content = models.CharField(max_length=500)
    pos_latitude = models.FloatField()
    pos_longiutde = models.FloatField()

