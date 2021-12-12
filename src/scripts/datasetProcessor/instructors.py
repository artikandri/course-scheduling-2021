import os
from pathlib import Path
import random
import string
import csv

# get project root, relative to this folder: if the folder changes, the path MUST be changed as well. 
PROJECT_ROOT = Path().resolve().parent.parent.parent

#initialize the directory
os.chdir(PROJECT_ROOT)
xlsx_file = Path('dataset/raw','dataset_pu-fal07-llr.xlsx')

#Generate new csv file
targetFilePath = Path("dataset/processed", "instructors.csv")
targetFile = open(targetFilePath, 'w', newline="")

# generate instructor initials
def generateInitial(): 
    firstInitial, secondInitial = random.choice(string.ascii_letters), random.choice(string.ascii_letters)
    initial = str(firstInitial+secondInitial).upper()
    return initial

# generate 100 random initials for the instructors
def generateInstructors():
    instructors = []
    for instructor in range(0, 150):
        initial = generateInitial()
        if initial not in instructors:
            instructors.append(initial)
            initial = generateInitial()

    return list(set(instructors))[:100]


def generatePreferences():
    preferences = []
    for i in range(0, 100):
        amountOfPreferences = random.randrange(0, 7, 1)
        preferences.append([random.randrange(1, 48, 1) for i in range(amountOfPreferences)])
    return preferences

instructors = generateInstructors()
preferences = generatePreferences()
preferencesInString = list(map(lambda pref: "".join(str(pref)), preferences))

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


