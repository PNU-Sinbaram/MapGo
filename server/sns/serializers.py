from .models import User, Post, PostImage, Comment, Like
from django.db import models
from rest_framework import serializers

class UserSerializer(serializers.HyperlinkedModelSerializer):
    picture = serializers.ImageField(use_url=True)

    class Meta:
        model = User
        fields = ('id', 'deviceID', 'username', 'picture')

class PostImageSerializer(serializers.ModelSerializer):
    image = serializers.ImageField(use_url=True)

    class Meta:
        model = PostImage
        fields = ('image', 'post')

class CommentSerializer(serializers.ModelSerializer):
    class Meta:
        model = Comment
        fields = ('writer', 'contents', 'post')

class LikeSerializer(serializers.ModelSerializer):
    class Meta:
        model = Like
        fields = ('liker', 'post')

class LocationSerializer(serializers.Serializer):
    lat = serializers.FloatField()
    lng = serializers.FloatField()

class PostSerializer(serializers.ModelSerializer):
    postImage = PostImageSerializer(many=True, read_only=True)
    writer = UserSerializer(read_only=True)
    comment = CommentSerializer(many=True, read_only=True)
    like = LikeSerializer(many=True, read_only=True)
    location = LocationSerializer()
    totalLikes = serializers.SerializerMethodField(read_only=True)

    def get_totalLikes(self, likes):
        return likes.like.count()

    class Meta:
        model = Post
        fields = ('postID', 'writer', 'contents', 'postImage', 'location', 'postTime', 'comment', 'like', 'totalLikes')

