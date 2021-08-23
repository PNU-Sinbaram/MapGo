from django.shortcuts import render

from rest_framework import viewsets

from .serializers import UserSerializer
from .models import User


# Create your views here.
class UserViewSet(viewsets.ModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserSerializer
