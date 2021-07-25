from django.db import models

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
