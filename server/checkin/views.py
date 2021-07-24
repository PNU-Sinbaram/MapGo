from django.shortcuts import render

from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.parsers import JSONParser

from .serializers import CheckinSerializer
from .models import Checkin

# Create your views here.

class CheckinViewSet(viewsets.ViewSet):
    def list(self, request):
        checkinList = Checkin.objects.all()
        serializer = CheckinSerializer(checkinList, many=True)
        return Response(serializer.data, status=200)

    def create(self, request):
        requestData = JSONParser().parse(request)
        serializer = CheckinSerializer(data=requestData)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=201)
        return Response(serializer.errors, status=400)

