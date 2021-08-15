from django.db import models
from django.core.validators import MaxValueValidator, MinValueValidator

# Create your models here.

class PlaceData(models.Model):
    name = models.CharField(max_length=30)
    long = models.FloatField()
    lat = models.FloatField()
    filtering = models.IntegerField()

    def __str__(self):
        return self.name

class STC(models.Model):
    recommend1 = models.JSONField()
    recommend2 = models.JSONField()

class PlaceRequestData(models.Model):
    User_ID = models.CharField(max_length=100)
    lat = models.FloatField(validators=[MinValueValidator(-85), MaxValueValidator(85)])
    long = models.FloatField(validators=[MinValueValidator(-180), MaxValueValidator(180)])
    keywords = models.CharField(max_length=50)
    epsilon = models.IntegerField(validators=[MinValueValidator(0), MaxValueValidator(100)])

