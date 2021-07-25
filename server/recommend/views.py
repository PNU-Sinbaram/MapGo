from django.shortcuts import render
from rest_framework import viewsets
from .serializers import PlaceDataSerializer, STCSerializer
from .models import PlaceData, STC 

# Create your views here.
class STCViewSet(viewsets.ModelViewSet):
    queryset = STC.objects.all()
    serializer_class = STCSerializer
