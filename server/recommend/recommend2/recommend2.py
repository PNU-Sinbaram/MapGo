import requests
import json
import os
import pandas as pd
from datetime import date


class recommend2: 
    def __init__(self):
        """Will be implemented later."""
        pass

    @classmethod
    def recommend(cls, UserID, lat, long, epsilon):
        """Returns list of place recommendation using NCF.

        Gets nearby place list from argument latitude, longitude using Google Place API,
        compares UserID, place list with NCF model prediction table output.

        Sorts comparison result as prediction probability, divide into percentage suggested by epsilon.
        Picks randomly 5 place list and returns as 2-dimension list.

        Args:
            UserID (string) : User ID(or device id) which will be the target of recommendation
            lat (float) : Latitude of current 'UserID' position
            long (float) : Longitude of current 'UserID' position
            epsilon (integer) : Percentage of recommedation scope

        Returns:
            2-D List : List of recommended place. Each element has place name, latitude, longitude, filtering
        """

        fileDirectory = os.path.dirname(__file__)
        df = pd.read_csv(fileDirectory+'/recommendTable_'+str(date.today())+'.csv') 
        df = df.set_index('Unnamed: 0')

        locationNameList = []
        locationPosList = []
        recommendResult = []
        
        location = str(lat)+','+str(long) 
        resp = cls.__getNearby(location)

        jsonObject = json.loads(resp.text)
        responseResult = jsonObject.get('results')
        for loc in responseResult:
            locationNameList.append(str(loc["vicinity"])+" "+str(loc["name"]))
            locationPosList.append(loc["geometry"]["location"])
        locationPosList = dict(zip(locationNameList, locationPosList))

        duplicatedColumn = set(df.columns.values.tolist()) & set(locationNameList)
        recommendPlaces = df[duplicatedColumn].loc[UserID].sort_values(axis=0, ascending=False)
        partialIndex = round(recommendPlaces.count()*(epsilon/100))
        if partialIndex < 5:
            partialIndex = 5
        recommendPlaces = recommendPlaces[:partialIndex]
        recommendPlaces = recommendPlaces.sample(n=5).sort_values(axis=0, ascending=False)
        for placeName in recommendPlaces.index:
            recommendResult.append({"name": placeName, 
                                    "lat": round(locationPosList[placeName]["lat"], 6), 
                                    "long": round(locationPosList[placeName]["lng"], 6), 
                                    "filtering": 2})

        return(recommendResult)

    def __getNearby(location):
        URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
        key = os.environ['GOOGLE_PLACES_KEY']
        params = {'key': key, 'location': location, 'radius': 100, 'language': 'ko'}
        response = requests.get(URL, params=params)
        return response

if __name__ == '__main__':
    print(recommend2.recommend("target", 35.231480, 129.085178, 100))
