from .models import User
from rest_framework import serializers

class UserSerializer(serializers.HyperlinkedModelSerializer):
    profileImage = serializers.ImageField(use_url=True)

    class Meta:
        model = User
        fields = ('userID', 'deviceID', 'username', 'profileImage')
