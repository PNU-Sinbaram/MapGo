from rest_framework import serializers
from .models import Checkin


class CheckinSerializer(serializers.ModelSerializer):
    class Meta:
        '''Create serializer for model Checkin'''
        model = Checkin
        fields = ('User_ID', 'lat', 'long', 'timeStamp')
