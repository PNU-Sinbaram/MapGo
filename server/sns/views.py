from django.shortcuts import render

from rest_framework import viewsets
from rest_framework.parsers import JSONParser
from rest_framework.response import Response

from .serializers import UserSerializer, PostSerializer, PostImageSerializer, CommentSerializer, LikeSerializer
from .models import User, Post, Like

import json

# Create your views here.
class UserViewSet(viewsets.ViewSet):
    def list(self, request, **kwargs):
        if kwargs.get('userID') is None:
            queryset = User.objects.all()
            serializer = UserSerializer(queryset, many=True)
            return Response(serializer.data, status=200)
        else:
            query = User.objects.get(userID=kwargs.get('userID'))
            serializer = UserSerializer(query)
            return Response(serializer.data, status=200)
    def create(self, request):
        requestData = {"userID": request.POST.get("userID"),
                       "deviceID": request.POST.get("deviceID"),
                       "username": request.POST.get("username"),
                       "profileImage": request.FILES['profileImage']}
        serializer = UserSerializer(data=requestData)
        if serializer.is_valid(): 
            serializer.save()
            return Response(serializer.data, status=200)
        return Response(serializer.errors, status=400)

class PostViewSet(viewsets.ViewSet):
    def list(self, request, **kwargs):
        if kwargs.get('author') is None:
            queryset = Post.objects.all()
            serializer = PostSerializer(queryset, many=True)
        else:
            Posts = Post.objects.filter(author=kwargs.get('author'))
            serializer = PostSerializer(Posts, many=True)
        return Response(serializer.data)

    def create(self, request):
        requestData = {"title": request.POST.get("title"),
                       "content": request.POST.get("content"),
                       "location": json.loads(request.POST.get("location"))}
        postSerializer = PostSerializer(data=requestData)
        if postSerializer.is_valid():
            postSerializer.validated_data['author'] = User.objects.get(userID=request.POST.get("author"))
            postSerializer.save()
            for image in request.FILES.getlist("postImage"):
                requestData_Image = {"post": postSerializer.data["postID"], "post_image": image}
                postimageSerializer = PostImageSerializer(data=requestData_Image)
                if postimageSerializer.is_valid():
                    postimageSerializer.save()

            return Response(postSerializer.data, status=200)
        return Response(postSerializer.errors, status=400)

class CommentViewSet(viewsets.ViewSet):
    def create(self, request, **kwargs):
        requestData = {"author": request.POST.get("userID"),
                       "post": kwargs.get('postID'),
                       "content": request.POST.get("content")}
        postid = kwargs.get('postID')
        postquery = Post.objects.get(postID=postid)
        userquery = User.objects.get(userID=request.POST.get("userID"))
        serializer = CommentSerializer(data=requestData)
        if serializer.is_valid():
            serializer.validated_data['author']=userquery
            serializer.validated_data['post']=postquery
            serializer.save()
            return Response(serializer.data, status=200)
        return Response(serializer.errors, status=400)

class LikeViewSet(viewsets.ViewSet):
    def create(self, request, **kwargs):
        requestData = {"liker": request.POST.get("userID"),
                       "post": kwargs.get('postID')}
        likerID = int(requestData["liker"])
        posts_forcheck = Like.objects.filter(post=kwargs.get("postID"))
        likerlist = []
        for post in posts_forcheck:
            likerlist.append(getattr(post, 'liker'))

        if likerID in likerlist:
            return Response("User Aleady liked to this post.", status=400)

        serializer = LikeSerializer(data=requestData)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=200)
        return Response(serializer.errors, status=400)
    def destroy(self, request, **kwargs):
        requestData = {"liker": request.POST.get("userID"),
                       "post": kwargs.get('postID')}
        likerID = int(requestData["liker"])
        posts = Like.objects.filter(post=kwargs.get("postID"))
        posts_liker = posts.filter(liker=likerID)
        if not posts_liker.exists():
            return Response(f'Can\'t find post {requestData["post"]} or userID {likerID}.', status=400)

        posts_liker.delete()
        return Response("deleted.", status=200)
