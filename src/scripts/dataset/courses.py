import os
from pathlib import Path
import pandas as pd
from random import randrange, choice
import csv

# get project root, relative to this folder: if the folder changes, the path MUST be changed as well. 
PROJECT_ROOT = Path().resolve().parent.parent.parent

#initialize the directory
os.chdir(PROJECT_ROOT)
datasetUnitimeFilePath = Path('dataset/raw','dataset_pu-fal07-llr.xlsx')
datasetIllinoisFilePath = Path('dataset/raw','dataset_illinois_2019-fa.csv')
instructorFilePath = Path('dataset/processed','instructors.csv')

# read unitime dataset file
datasetUnitime = pd.read_excel(datasetUnitimeFilePath, sheet_name='Sheet1', usecols="AO, AP, AQ")
datasetUnitime.dropna(inplace=True)

# read instructor file
instructorFile = pd.read_csv(instructorFilePath, usecols = ['instructors'], sep=";")
instructors = instructorFile["instructors"].to_list()

# read illinois dataset file
datasetIllinois = pd.read_csv(datasetIllinoisFilePath, usecols = ['Name'], sep=",").drop_duplicates()
coursesNames = datasetIllinois['Name'].to_list()

# find how many times the courses are scheduled weekly
# AO (days) column stores the number of days the courses are held
# AQ (length) column stores the number of hours of each course
def findWeeklyHours():
    weeklySchedules = datasetUnitime['days']
    durationInFiveMinutesUnit = datasetUnitime['length']
    weeklySchedulesCounts = list(map(lambda schedule:str(schedule).count("1"), weeklySchedules))
    durationInHours = list(map(lambda hours:hours/12, durationInFiveMinutesUnit))

    weeklyHours = list()
    for index, scheduleCount in enumerate(weeklySchedulesCounts):
        weeklyHours.append(scheduleCount*durationInHours[index])
    
    return weeklyHours

# generate weekly hours
weeklyHours = findWeeklyHours()

# get courses name from illinois dataset and limit the length to the number of courses in Unitime
courses = coursesNames[:len(weeklyHours)]

# generate a list of course instructor ids
courseInstructors = list()
for i in range (0, len(weeklyHours)):
    courseInstructors.append(randrange(0, len(instructors), 1))

#Generate new csv file
targetFilePath = Path("dataset/processed", "courses.csv")
targetFile = open(targetFilePath, 'w', newline="")

# write the file and close 
writer = csv.writer(targetFile, dialect="excel", delimiter=";")
writer.writerow(["courses", "weekly_hours", "instructors"])
i = 0
while i!=len(courses):
    writer.writerow([courses[i:i+1], weeklyHours[i:i+1], courseInstructors[i:i+1]])
    i+=1
targetFile.close()

# replace quotation marks
with open(targetFilePath, "r+", encoding="utf-8") as csv_file:
    content = csv_file.read()

with open(targetFilePath, "w+", encoding="utf-8") as csv_file:
    newContent = content.replace('"', '').replace("[", "").replace("]", "").replace("\'", "").replace(", ", ",")
    csv_file.write(newContent)

    


