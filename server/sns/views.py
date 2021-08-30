from django.core.exceptions import ObjectDoesNotExist

from rest_framework import viewsets
from rest_framework.response import Response

from .serializers import UserSerializer, PostSerializer, \
                         PostImageSerializer, CommentSerializer, LikeSerializer
from .models import User, Post, Like

import json


# Create your views here.
class UserViewSet(viewsets.ViewSet):
    @classmethod
    def list(self, request, **kwargs):
        if kwargs.get('deviceID') is None:
            queryset = User.objects.all()
            serializer = UserSerializer(queryset, many=True)
            return Response(serializer.data, status=200)
        else:
            try:
                query = User.objects.get(deviceID=kwargs.get('deviceID'))
            except ObjectDoesNotExist:
                return Response(f'The user who has device ID '
                                f'[{kwargs.get("deviceID")}] '
                                f'doesn\'t exist.', status=400)
            serializer = UserSerializer(query)
            return Response(serializer.data, status=200)

    @classmethod
    def create(self, request):
        requestData = {"userID": request.POST.get("userID"),
                       "deviceID": request.POST.get("deviceID"),
                       "username": request.POST.get("username"),
                       "picture": request.FILES['picture']}
        serializer = UserSerializer(data=requestData)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=201)
        return Response(serializer.errors, status=400)


class PostViewSet(viewsets.ViewSet):
    @classmethod
    def list(self, request, **kwargs):
        if kwargs.get('author') is None:
            queryset = Post.objects.all()
            serializer = PostSerializer(queryset, many=True)
        else:
            Posts = Post.objects.filter(author=kwargs.get('author'))
            serializer = PostSerializer(Posts, many=True)
        return Response(serializer.data)

    @classmethod
    def create(self, request):
        requestData = {"contents": request.POST.get("contents"),
                       "location": json.loads(request.POST.get("location"))}
        postSerializer = PostSerializer(data=requestData)
        if postSerializer.is_valid():
            try:
                postSerializer.validated_data['writer'] = \
                    User.objects.get(id=request.POST.get("writer"))
            except ObjectDoesNotExist:
                return Response(f'The user with id '
                                f'[{request.POST.get("writer")}] '
                                f'doesn\'t exist.', status=400)
            postSerializer.save()
            for image in request.FILES.getlist("postImage"):
                requestData_Image = {"post": postSerializer.data["postID"],
                                     "image": image}
                postimageSerializer = \
                    PostImageSerializer(data=requestData_Image)
                if postimageSerializer.is_valid():
                    postimageSerializer.save()

            return Response(postSerializer.data, status=201)
        return Response(postSerializer.errors, status=400)


class CommentViewSet(viewsets.ViewSet):
    @classmethod
    def create(self, request, **kwargs):
        requestData = {"writer": request.POST.get("writer"),
                       "post": kwargs.get('postID'),
                       "contents": request.POST.get("contents")}
        postid = kwargs.get('postID')
        try:
            postquery = Post.objects.get(postID=postid)
            userquery = User.objects.get(id=request.POST.get("writer"))
        except ObjectDoesNotExist as e:
            return Response(str(e), status=400)

        serializer = CommentSerializer(data=requestData)
        if serializer.is_valid():
            serializer.validated_data['writer'] = userquery
            serializer.validated_data['post'] = postquery
            serializer.save()
            return Response(serializer.data, status=201)
        return Response(serializer.errors, status=400)


class LikeViewSet(viewsets.ViewSet):
    @classmethod
    def create(self, request, **kwargs):
        requestData = {"liker": request.POST.get("userID"),
                       "post": kwargs.get('postID')}
        likerID = int(requestData["liker"])
        posts = Like.objects.filter(post=kwargs.get("postID"))
        posts_liker = posts.filter(liker=likerID)
        if posts_liker.exists():
            return Response("User Aleady liked to this post.", status=400)

        serializer = LikeSerializer(data=requestData)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=200)
        return Response(serializer.errors, status=400)

    @classmethod
    def destroy(self, request, **kwargs):
        requestData = {"liker": request.POST.get("userID"),
                       "post": kwargs.get('postID')}
        likerID = int(requestData["liker"])
        posts = Like.objects.filter(post=kwargs.get("postID"))
        posts_liker = posts.filter(liker=likerID)
        if not posts_liker.exists():
            return Response(f'Can\'t find post {requestData["post"]}'
                            f' or userID {likerID}.', status=400)

        posts_liker.delete()
        return Response("deleted.", status=200)
