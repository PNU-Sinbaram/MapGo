from django.shortcuts import render

from rest_framework import viewsets
from rest_framework.parsers import JSONParser
from rest_framework.response import Response

from .serializers import UserSerializer, PostSerializer, PostImageSerializer
from .models import User, Post

# Create your views here.
class UserViewSet(viewsets.ViewSet):
    def list(self, request):
        queryset = User.objects.all()
        serializer = UserSerializer(queryset, many=True)
        return Response(serializer.data)
    def create(self, request):
        requestData = {"userID": request.POST.get("userID"),
                       "deviceID": request.POST.get("deviceID"),
                       "username": request.POST.get("username"),
                       "profileImage": request.FILES['profileImage']}
        print(request.FILES['profileImage'])
        print(requestData)
        serializer = UserSerializer(data=requestData)
        if serializer.is_valid(): 
            serializer.save()
            return Response(serializer.data, status=200)
        return Response(serializer.errors, status=400)

class PostViewSet(viewsets.ViewSet):
    def list(self, request):
        queryset = Post.objects.all()
        serializer = PostSerializer(queryset, many=True)
        return Response(serializer.data)

    def create(self, request):
        requestData = {"title": request.POST.get("title"),
                       "content": request.POST.get("content"),
                       "pos_latitude": request.POST.get("pos_latitude"),
                       "pos_longitude": request.POST.get("pos_longitude")}
        postSerializer = PostSerializer(data=requestData)
        if postSerializer.is_valid():
            postSerializer.save()
            for image in request.FILES.getlist("postImage"):
                requestData_Image = {"post": postSerializer.data["postID"], "post_image": image}
                postimageSerializer = PostImageSerializer(data=requestData_Image)
                if postimageSerializer.is_valid():
                    postimageSerializer.save()

            return Response(postSerializer.data, status=200)
        return Response(postSerializer.errors, status=400)
