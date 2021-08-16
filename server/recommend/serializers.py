from rest_framework import serializers
from .models import STC, PlaceData, PlaceRequestData


class PlaceDataSerializer(serializers.ModelSerializer):
    class Meta:

        """Make serializer for model PlaceData"""
        model = PlaceData
        fields = ('id', 'name', 'long', 'lat', 'filtering')


class STCSerializer(serializers.ModelSerializer):
    id = serializers.IntegerField()
    recommend1 = PlaceDataSerializer()
    recommend2 = PlaceDataSerializer()

    class Meta:

        """Make serializer for model STCSerializer"""
        model = STC
        fields = ('id', 'recommend1', 'recommend2')


class PlaceRequestSerializer(serializers.ModelSerializer):
    class Meta:

        """Make serializer for model PlaceRequestData"""
        model = PlaceRequestData
        fields = ('User_ID', 'lat', 'long', 'keywords', 'epsilon')
