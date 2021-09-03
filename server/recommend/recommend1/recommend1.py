# -*-coding:utf-8-*-

import os
import sys
import urllib.request
import json
import random
import config
from googletrans import Translator


class recommend1:
    def __init__(self):
        self.radius = 100
        self.key = os.environ["GOOGLE_PLACES_KEY"]

    def call_api(self, lat, lng):
        url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"\
                   + "location=" + str(lat) + "," + str(lng)\
                   + "&radius=" + str(self.radius)\
                   + "&key=" + self.key

        request = urllib.request.Request(url)
        response = urllib.request.urlopen(request)

        rescode = response.getcode()

        if(rescode == 200):
            response_body = response.read()
            return(response_body.decode('utf-8'))
        else:
            print("Error Code:" + rescode)

    def call_review(self, place_key):
        url = "https://maps.googleapis.com/maps/api/place/details/json?"\
              + "place_id=" + place_key\
              + "&fields=name,rating,review"\
              + "&key=" + self.key

        request = urllib.request.Request(url)
        response = urllib.request.urlopen(request)

        rescode = response.getcode()

        if(rescode == 200):
            response_body = response.read()
            return(response_body.decode('utf-8'))
        else:
            print("Error Code:" + rescode)

    def LocalAlignment(self, String1, String2, GapPenalty):
        Match, MisMatch = 15, -5
        DP = []
        for i in range(len(String2)+1):
            DP.append([0]*(len(String1)+1))

        for y in range(len(String2)):
            for x in range(len(String1)):
                MatchTest = 0

                if(String1[x] == String2[y]):
                    MatchTest = Match
                else:
                    MatchTest = MisMatch

                DP[y+1][x+1] = max(DP[y][x] + MatchTest,
                                   DP[y][x+1] + GapPenalty,
                                   DP[y+1][x] + GapPenalty, 0)

        return DP

    def word_tanslate(self, word):
        translator = Translator()
        result = translator.translate(word, dest="en")
        return result.text

    def json2list(self, js):
        return json.loads(js)

    def recommend(self, lat, lng, keyword, epsilon):
        filtering_result = []
        translated_keyword = self.word_tanslate(keyword)

        places_info = []
        api_result = self.call_api(lat, lng)
        places = self.json2list(api_result)
        # analyze each place infomation
        for place in places['results']:
            place_info = []

            place_info.append(place['name'])
            place_info.append(place['place_id'])

            try:
                if(place['business_status'] != 'OPERATIONAL'):
                    continue
            except Exception:
                continue

            place_info.append(place['geometry']['location']['lat'])
            place_info.append(place['geometry']['location']['lng'])

            places_info.append(place_info)

        for x in range(len(places_info)):
            place_key = places_info[x][1]
            review_result = self.call_review(place_key)
            reviews = self.json2list(review_result)
            alig_max = 0
            for y in range(len(reviews['result'])):
                try:
                    reviewtext = reviews['result']['reviews'][y]['text']
                    reviewtext = reviewtext.replace(" ", "").lower()
                    transtext = translated_keyword.replace(" ", "").lower()
                    alig_score = self.LocalAlignment(reviewtext,
                                                     transtext,
                                                     (epsilon-101))
                    if(max(map(max, alig_score)) > alig_max):
                        alig_max = max(map(max, alig_score))
                except Exception:
                    break
            places_info[x].insert(0, alig_max)
        places_info.sort(reverse=True)

        while(len(filtering_result) < 5):
            try:
                rank_place = places_info.pop(0)
                filtering_result.append({"name": rank_place[1],
                                         "lat": rank_place[3],
                                         "long": rank_place[4],
                                         "filtering": 1})
            except Exception:
                break

        return(filtering_result)
