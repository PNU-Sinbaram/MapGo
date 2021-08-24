from .models import User, Post, PostImage
from django.db import models
from rest_framework import serializers

class UserSerializer(serializers.HyperlinkedModelSerializer):
    profileImage = serializers.ImageField(use_url=True)

    class Meta:
        model = User
        fields = ('userID', 'deviceID', 'username', 'profileImage')

class PostImageSerializer(serializers.ModelSerializer):
    post_image = serializers.ImageField(use_url=True)

    class Meta:
        model = PostImage
        fields = ('post', 'post_image')

class PostSerializer(serializers.ModelSerializer):
    postImage = PostImageSerializer(many=True, read_only=True)

    class Meta:
        model = Post
        fields = ('postID', 'content', 'pos_latitude', 'pos_longitude', 'postImage')
