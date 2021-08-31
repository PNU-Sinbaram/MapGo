# from django.shortcuts import render
from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.parsers import JSONParser

from .serializers import PlaceRequestSerializer

from .recommend1.recommend1 import recommend1
from .recommend2.recommend2 import recommend2

import json


# Create your views here.
class STCViewSet(viewsets.ViewSet):
    @classmethod
    def list(self, request):
        # recommender1 = recommend1()
        # recommender2 = recommend2()
        UserID = request.query_params["User_ID"]
        latitude = request.query_params["lat"]
        longitude = request.query_params["long"]
        keywords = request.query_params["keywords"]
        epsilon = request.query_params["epsilon"]

        requestData = {"User_ID": UserID,
                       "lat": latitude,
                       "long": longitude,
                       "keywords": keywords,
                       "epsilon": epsilon}
        serializer = PlaceRequestSerializer(data=requestData)
        if serializer.is_valid():
            requestResult1 = recommend1.recommend(
                latitude, longitude, keywords, epsilon
            )
            requestResult2 = recommend2.recommend(
                UserID, latitude, longitude, epsilon
            )

            if requestResult2 is not None:
                result = {"result": requestResult1+requestResult2}
            else:
                result = {"result": requestResult1}
            jsonRequest = json.dumps(result, ensure_ascii=False)

            return Response(
                json.JSONDecoder().decode(jsonRequest)["result"],
                status=200
            )
        return Response(serializer.errors, status=400)
