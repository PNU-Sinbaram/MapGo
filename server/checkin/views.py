# from django.shortcuts import render
from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.parsers import JSONParser

from .serializers import CheckinSerializer
from .models import Checkin

from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator
from django.core.exceptions import ObjectDoesNotExist

import json
import os
import requests


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
            placeList = []

            locationString = str(serializer.validated_data['lat'])+','+str(serializer.validated_data['long'])
            googlePlaceApiKey = os.environ['GOOGLE_PLACES_KEY']
            URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
            params = {'key': googlePlaceApiKey, 'location': locationString, 'radius': 10, 'language': 'ko'}
            resp = json.loads(requests.get(URL, params=params).text)
            resp = resp.get('results')

            for place in resp:
                if place["vicinity"] != place["name"]:
                    placeList.append(place["vicinity"]+" "+place["name"])

            if serializer.validated_data.get('placeName', None) is None:
                if len(placeList) == 0:
                    print(resp)
                    return Response("There are no places in "+locationString, status=400)
                elif len(placeList) == 1:
                    serializer.validated_data['placeName'] = placeList[0]
                    serializer.save()
                    return Response(serializer.data, status=200)
                else:
                    placeNameListJSON = json.dumps({"message": 'There are multiple location in requested position. Re-request with field "placeName"',
                                                    "places": self.__locationListToJSON(placeList)}, ensure_ascii=False)
                    print(placeNameListJSON)
                    return Response(json.JSONDecoder().decode(placeNameListJSON), status=203)
            else:
                if serializer.validated_data.get('placeName') in placeList:
                    serializer.save()
                    return Response(serializer.data, status=200)
                else:
                    return Response("There is no place.", status=400)

        return Response(serializer.errors, status=400)

    @classmethod
    def delete(self, request, userid):
        try:
            userObject = Checkin.objects.filter(User_ID=userid)
            userObject.delete()

            return Response("Delete : "+userid, status=200)
        except ObjectDoesNotExist:
            return Response("User "+userid+" not found.", status=400)

    def __locationListToJSON(locationList):
        locId = 1
        placeNameList = []
        for l in locationList:
            placeNameList.append({'id': locId, 'placeName': l})
            locId+=1
        return placeNameList
