# from django.shortcuts import render
from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.parsers import JSONParser

from .serializers import PlaceRequestSerializer

from .recommend1.recommend1 import recommend1
from .recommend2.recommend2 import recommend2

import json


# Create your views here.
class STCViewSet(viewsets.ModelViewSet):
    @classmethod
    def list(self, request):
        # recommender1 = recommend1()
        # recommender2 = recommend2()

        requestData = JSONParser().parse(request)
        serializer = PlaceRequestSerializer(data=requestData)
        if serializer.is_valid():
            UserID = requestData['User_ID']
            latitude = requestData['lat']
            longitude = requestData['long']
            keywords = requestData['keywords']
            epsilon = requestData['epsilon']

            requestResult1 = recommend1.recommend(
                latitude, longitude, keywords, epsilon
            )
            requestResult2 = recommend2.recommend(
                UserID, latitude, longitude, epsilon
            )

            result = {"result": requestResult1+requestResult2}
            jsonRequest = json.dumps(result, ensure_ascii=False)

            return Response(jsonRequest, status=200)
        return Response(serializer.errors, status=400)
