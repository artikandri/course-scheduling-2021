import os
from pathlib import Path
import random
import pandas as pd
import string
import csv

# get project root, relative to this folder: if the folder changes, the path MUST be changed as well. 
PROJECT_ROOT = Path().resolve().parent.parent.parent

#initialize the directory
os.chdir(PROJECT_ROOT)
xlsx_file = Path('dataset/raw','dataset_pu-fal07-llr.xlsx')
timeslotFilePath = Path('dataset/processed','timeslots.csv')

# read unitime dataset file
unitimeInstructors = pd.read_excel(xlsx_file, sheet_name='Sheet1', usecols="AJ")
unitimeInstructors.dropna(inplace=True)

# read timeslot file
timeslotFile = pd.read_csv(timeslotFilePath, usecols=['timeslots'], sep=";")
timeslots = timeslotFile["timeslots"].to_list()

#Generate new csv file
targetFilePath = Path("dataset/processed", "instructors.csv")
targetFile = open(targetFilePath, 'w', newline="")

# generate random initials
def generateInitial(): 
    firstInitial, secondInitial = random.choice(string.ascii_letters), random.choice(string.ascii_letters)
    initial = str(firstInitial+secondInitial).upper()
    return initial

# Relabel Unitime instructors and added preferences
# preferences refer to timeslots index and started from 0
def generateInstructors():
    instructors = []
    for instructor in range(len(unitimeInstructors)-1):
        amountOfPreferences = random.randrange(0, 7, 1)
        instructors.append([generateInitial(), [random.randrange(0, len(timeslots)-1, 1) for i in range(amountOfPreferences)]])

    return instructors

instructorsAndPreferences = generateInstructors()
instructors, preferences=zip(*instructorsAndPreferences)
preferencesInString = list(map(lambda pref: "".join(str(pref)), preferences))
instructors = list(instructors)

# write the file and close 
writer = csv.writer(targetFile, dialect="excel", delimiter=";")
writer.writerow(["instructors", "preferences"])
i = 0
while i!=len(instructors):
    writer.writerow([instructors[i:i+1], preferencesInString[i:i+1]])
    i+=1
targetFile.close()

# replace quotation marks
with open(targetFilePath, "r+", encoding="utf-8") as csv_file:
    content = csv_file.read()

with open(targetFilePath, "w+", encoding="utf-8") as csv_file:
    newContent = content.replace('"', '').replace("[", "").replace("]", "").replace("\'", "").replace(", ", ",")
    csv_file.write(newContent)


