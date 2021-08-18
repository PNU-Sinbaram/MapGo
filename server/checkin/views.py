# from django.shortcuts import render
from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.parsers import JSONParser

from .serializers import CheckinSerializer
from .models import Checkin

from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator
from django.core.exceptions import ObjectDoesNotExist


# Create your views here.
@method_decorator(csrf_exempt, name='delete')
class CheckinViewSet(viewsets.ViewSet):
    @classmethod
    def list(self, request):
        checkinList = Checkin.objects.all()
        serializer = CheckinSerializer(checkinList, many=True)
        return Response(serializer.data, status=200)

    @classmethod
    def create(self, request):
        requestData = JSONParser().parse(request)
        serializer = CheckinSerializer(data=requestData)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=201)
        return Response(serializer.errors, status=400)

    @classmethod
    def delete(self, request, userid):
        try:
            userObject = Checkin.objects.get(User_ID=userid)
            userObject.delete()

            return Response("Delete : "+userid, status=200)
        except ObjectDoesNotExist:
            return Response("User "+userid+" not found.", status=400)
