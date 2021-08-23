from django.shortcuts import render

from rest_framework import viewsets
from rest_framework.parsers import JSONParser
from rest_framework.response import Response

from .serializers import UserSerializer
from .models import User

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
