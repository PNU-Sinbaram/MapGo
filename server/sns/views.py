from django.shortcuts import render

from rest_framework import viewsets
from rest_framework.parsers import JSONParser
from rest_framework.response import Response

from .serializers import UserSerializer, PostSerializer, PostImageSerializer, CommentSerializer, LikeSerializer
from .models import User, Post, Like

import json

# Create your views here.
class UserViewSet(viewsets.ModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserSerializer

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
        requestData = {"contents": request.POST.get("contents"),
                       "location": json.loads(request.POST.get("location"))}
        postSerializer = PostSerializer(data=requestData)
        if postSerializer.is_valid():
            postSerializer.validated_data['writer'] = User.objects.get(id=request.POST.get("writer"))
            postSerializer.save()
            for image in request.FILES.getlist("postImage"):
                requestData_Image = {"post": postSerializer.data["postID"], "image": image}
                postimageSerializer = PostImageSerializer(data=requestData_Image)
                if postimageSerializer.is_valid():
                    postimageSerializer.save()

            return Response(postSerializer.data, status=200)
        return Response(postSerializer.errors, status=400)

class CommentViewSet(viewsets.ViewSet):
    def create(self, request, **kwargs):
        requestData = {"writer": request.POST.get("writer"),
                       "post": kwargs.get('postID'),
                       "contents": request.POST.get("contents")}
        postid = kwargs.get('postID')
        postquery = Post.objects.get(postID=postid)
        userquery = User.objects.get(id=request.POST.get("writer"))
        serializer = CommentSerializer(data=requestData)
        if serializer.is_valid():
            serializer.validated_data['writer']=userquery
            serializer.validated_data['post']=postquery
            serializer.save()
            return Response(serializer.data, status=200)
        return Response(serializer.errors, status=400)

class LikeViewSet(viewsets.ViewSet):
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
