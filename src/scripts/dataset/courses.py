import os
from pathlib import Path
import pandas as pd
from random import randrange, choice
import csv

# get project root, relative to this folder: if the folder changes, the path MUST be changed as well.
PROJECT_ROOT = Path().resolve().parent.parent.parent

# initialize the directory
os.chdir(PROJECT_ROOT)
datasetUnitimeFilePath = Path('dataset/raw', 'dataset_pu-fal07-llr.xlsx')
datasetIllinoisFilePath = Path('dataset/raw', 'dataset_illinois_2019-fa.csv')
instructorFilePath = Path('dataset/processed', 'instructors.csv')

# read unitime dataset file
# W (value.2) column stores department value
# AB (offering) column stores course offerings
# AG (scheduler) column stores department id for each course
# AO (days) column stores the number of days the courses are held
# AQ (length) column stores the number of hours of each course
datasetUnitime = pd.read_excel(
    datasetUnitimeFilePath, sheet_name='Sheet1', usecols="W, AB, AG, AO, AQ",   header=0, converters={"value.2": str})
datasetUnitime.rename(columns = {'value.1':'value', "value.2": "value"},inplace =True) 

# get unique list of unitime depts. Ideally, this list should be taken from AG column, but this column only has 1 department (3)
unitimeDepts = list(set(datasetUnitime['value'].dropna()))

# get unique list of unitime courses
unitimeCourses = list(set(datasetUnitime['offering'].dropna()))


# read instructor file
instructorFile = pd.read_csv(instructorFilePath, usecols=['instructors'], sep=";")
instructors = instructorFile["instructors"].to_list()

# read illinois dataset file
datasetIllinois = pd.read_csv(datasetIllinoisFilePath, usecols=[
                              'Name'], sep=",").drop_duplicates()
coursesNames = datasetIllinois['Name'].to_list()

# find how many times the courses are scheduled weekly
def findWeeklyHours():
    weeklySchedules = datasetUnitime['days'].dropna()
    durationInFiveMinutesUnit = datasetUnitime['length'].dropna()
    weeklySchedulesCounts = list(
        map(lambda schedule: str(schedule).count("1"), weeklySchedules))
    durationInHours = list(
        map(lambda hours: hours/12, durationInFiveMinutesUnit))

    weeklyHours = list()
    for index, scheduleCount in enumerate(weeklySchedulesCounts):
        weeklyHours.append(scheduleCount*durationInHours[index])

    return weeklyHours


# generate weekly hours
weeklyHours = findWeeklyHours()

# get courses, removes &amp;, limit the length, remove duplicates 
uniqueCourses = list()
for index, course in enumerate(coursesNames):
    if course not in uniqueCourses: 
        uniqueCourses.append(course)

uniqueCourses = list(map(lambda course:course.replace("&amp;", "&"), uniqueCourses))

courses = uniqueCourses[:len(unitimeCourses)]

# Generate new csv file
targetFilePath = Path("dataset/processed", "courses.csv")
targetFile = open(targetFilePath, 'w', newline="")

# write the file and close
writer = csv.writer(targetFile, dialect="excel", delimiter=";")
writer.writerow(["courses", "weekly_hours", "instructors", "major"])
i = 0
while i != len(courses):
    randomInstructor = randrange(0, len(instructors)-1, 1)
    randomDept = randrange(0, len(unitimeDepts)-1, 1)
    writer.writerow([courses[i:i+1], weeklyHours[i:i+1],
                    randomInstructor, randomDept])
    i += 1
targetFile.close()

# replace quotation marks
with open(targetFilePath, "r+", encoding="utf-8") as csv_file:
    content = csv_file.read()

with open(targetFilePath, "w+", encoding="utf-8") as csv_file:
    newContent = content.replace('"', '').replace(
        "[", "").replace("]", "").replace("\'", "").replace(", ", ",")
    csv_file.write(newContent)
