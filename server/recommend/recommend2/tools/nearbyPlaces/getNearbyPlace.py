import requests
import time
import json
import os
from tqdm import tqdm
import pandas as pd

def searchPlace():
    '''Get all places' name and vicinity around GPS location point from list of location point.

    List of location point consists of strings in form [latitude,longitude]. (without square brackets)
    From each point, Google Places API gets all places info around 70 meters, which ranges circle.

    Current location file almost covers area of 부산광역시 금정구 장전동.
    latitude-890 makes area circle move down, longitude+1123 makes area circle move right.
    '''

    filename = "./location"
    f = open(filename, "r")

    locationList = []

    key = os.environ['GOOGLE_PLACES_KEY']

    for location in tqdm(f.readlines()):
        locationString = location.strip()

        URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
        params = {'key': key, 'location': locationString, 'radius': 70, 'language': 'ko'}
        resp = requests.get(URL, params=params)

        jsonObject = json.loads(resp.text)
        responseResult = jsonObject.get('results')
        nextpageToken = jsonObject.get('next_page_token')
        for loc in responseResult:
            locationList.append([loc["vicinity"], loc["name"]])

        while nextpageToken is not None:
            time.sleep(2)
            params = {'key': key, 'pagetoken': nextpageToken, 'language': 'ko'}

            resp = requests.get(URL, params=params)

            jsonObject = json.loads(resp.text)
            responseResult = jsonObject.get('results')
            nextpageToken = jsonObject.get('next_page_token')
            for loc in responseResult:
                locationList.append([loc["vicinity"], loc["name"]])


    df = pd.DataFrame(locationList, columns=['vicinity', 'name'])
    df = df.drop_duplicates().sort_values(by=['name'], axis=0)
    print(df)
    df.to_csv("./getNearbyPlacesResult.csv")


if __name__ == "__main__":
    searchPlace()
