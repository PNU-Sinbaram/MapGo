from .models import User, Post, PostImage, Comment, Like
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

class CommentSerializer(serializers.ModelSerializer):
    class Meta:
        model = Comment
        fields = ('author', 'post', 'content')

class LikeSerializer(serializers.ModelSerializer):
    class Meta:
        model = Like
        fields = ('post', 'liker')

class PostSerializer(serializers.ModelSerializer):
    postImage = PostImageSerializer(many=True, read_only=True)
    author = UserSerializer(read_only=True)
    comment = CommentSerializer(many=True, read_only=True)
    like = LikeSerializer(many=True, read_only=True)

    class Meta:
        model = Post
        fields = ('postID', 'content', 'author', 'comment', 'like', 'pos_latitude', 'pos_longitude', 'postImage')

