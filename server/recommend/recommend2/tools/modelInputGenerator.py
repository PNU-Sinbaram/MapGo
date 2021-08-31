import pandas as pd
import sqlite3
import numpy as np
from reviewerHash import hashString


def scraperDataPreprocessor():
    scraperResult = open('./GoogleMapsReviewScraper/reviewer.txt',
                         encoding='UTF8')
    locationList = []

    for line in scraperResult.read().splitlines():
        if line[0] == '=':
            place = line[6:-6]
        else:
            locationList.append([place, line])

    df = pd.DataFrame(locationList, columns=['place', 'reviewer'])
    df = df[df.reviewer != 'No Reviews.']
    df['reviewer'] = df['reviewer'].apply(hashString)
    return df


def dbDataPreprocessor():
    conn = sqlite3.connect('../../../db.sqlite3')
    c = conn.cursor()

    c.execute("SELECT * FROM checkin_checkin")
    rows = c.fetchall()
    columns = [columnName[0] for columnName in c.description]

    df = pd.DataFrame.from_records(data=rows, columns=columns)

    requiredColumn = ["placeName", "User_ID"]
    df = df.reindex(columns=requiredColumn)
    df.rename(columns={"placeName": "place",
                       "User_ID": "reviewer"},
              inplace=True)

    conn.close()
    return df


if __name__ == '__main__':
    scraperDataframe = scraperDataPreprocessor()
    dbDataframe = dbDataPreprocessor()
    checkinDataframe = pd.concat([scraperDataframe, dbDataframe])
    checkinDataframe.to_csv('../checkin_history')
