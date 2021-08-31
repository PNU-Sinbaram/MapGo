from .models import User, Post, PostImage, Comment, Like
from rest_framework import serializers


class UserSerializer(serializers.HyperlinkedModelSerializer):
    picture = serializers.ImageField(use_url=True)

    class Meta:

        """Create serializer for model User"""
        model = User
        fields = ('id', 'deviceID', 'username', 'picture')


class PostImageSerializer(serializers.ModelSerializer):
    image = serializers.ImageField(use_url=True)

    class Meta:

        """Create serializer for image in model Post"""
        model = PostImage
        fields = ('image', 'post')


class CommentSerializer(serializers.ModelSerializer):
    writer = UserSerializer(read_only=True)

    class Meta:

        """Create serializer for comment in model Post"""
        model = Comment
        fields = ('writer', 'contents', 'post')


class LikeSerializer(serializers.ModelSerializer):
    class Meta:

        """Create serializer for like in model Post"""
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

    @staticmethod
    def get_totalLikes(likes):
        return likes.like.count()

    class Meta:

        """Create serializer for model Post"""
        model = Post
        fields = ('postID', 'writer', 'contents', 'postImage', 'location',
                  'postTime', 'comment', 'like', 'totalLikes')
