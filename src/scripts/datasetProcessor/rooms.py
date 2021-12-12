import os
from pathlib import Path
from random import choice
import string
import pandas as pd
import csv

# get project root, relative to this folder: if the folder changes, the path MUST be changed as well. 
PROJECT_ROOT = Path().resolve().parent.parent.parent

#initialize the directory
os.chdir(PROJECT_ROOT)
xlsx_file = Path('dataset/raw','dataset_pu-fal07-llr.xlsx')

# Open the Workbook
df = pd.read_excel(xlsx_file,sheet_name='Sheet1', usecols="K")
df.dropna(inplace=True)

# Generate room names
def generateRoomNames():
    numberOfRoomsInOriginalWorksheet = len(df)-1
    names = list()
    for x in range (1, numberOfRoomsInOriginalWorksheet):
        roomName = str("Room "+str(x)+choice(string.ascii_letters).upper())
        names.append(roomName)
    return names    

# remove duplicates
rooms = list(set(generateRoomNames()))

#Generate csv file for rooms
targetFilePath = Path("dataset/processed", "rooms.csv")
targetFile = open(targetFilePath, 'w', newline="")

# write the file and close 
writer = csv.writer(targetFile, dialect="excel", delimiter=";")
writer.writerow(["rooms"])
i = 0
while i!=len(rooms):
    writer.writerow(rooms[i:i+1])
    i+=1

# close file
targetFile.close()
