from django.db import models
from django.core.validators import MaxValueValidator, MinValueValidator


# Create your models here.
class Checkin(models.Model):
    User_ID = models.CharField(max_length=30)
    lat = models.FloatField(validators=[MinValueValidator(-85),
                                        MaxValueValidator(85)])
    long = models.FloatField(validators=[MinValueValidator(-180),
                                         MaxValueValidator(180)])
    timeStamp = models.DateTimeField(auto_now_add=True)
