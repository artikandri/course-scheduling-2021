import os
from pathlib import Path
from random import randrange, choice
import csv
from datetime import timedelta, datetime


# get project root, relative to this folder: if the folder changes, the path MUST be changed as well. 
PROJECT_ROOT = Path().resolve().parent.parent

#initialize the directory
os.chdir(PROJECT_ROOT)

# Generate the timeslot
# Unitime dataset defines timeslot everyday from Monday to Friday, from 07.00 to 19.00 (12 hrs)
# the timerange varies from 60 min to 90 min (1 to 1.5 hours) everyday

#sum original time with given duration
def sumTime (originalTime, timeToBeSummed):
    summedTime = timedelta()
    (hOr, mOr, sOr) = originalTime.split(':')
    (hSum, mSum, sSum) = str(timeToBeSummed).split(':')
    datetimeOr = timedelta(hours=int(hOr), minutes=int(mOr), seconds=int(sOr)) 
    datetimeSum = timedelta(hours=int(hSum), minutes=int(mSum), seconds=int(sSum))

    summedTime = summedTime + datetimeOr + datetimeSum
    splittedSummedTime = str(summedTime).split(":")
    summedTimeInHhMmFormat = splittedSummedTime[0]+":"+splittedSummedTime[1]
    return summedTimeInHhMmFormat

# generate random timerange for all working days, duration varies from 1 to 1.5 hours
def generateTimerange ():
    timeranges = []
    for i in range (0, 5):
        timerange = []
        amountOf90MinsClasses = [2, 4, 6, 8]

        for j in range(0, choice(amountOf90MinsClasses)): 
            timerange.append(1.5)
        amountOfClassesLeft = 12 - sum(timerange)

        for k in range(0, int(amountOfClassesLeft)):
            timerange.append(1)
        timeranges.append(timerange)
    return timeranges

# generate timeslots for all working days
def generateTimeslots ():
    days = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]
    timeranges = generateTimerange()
    timeslots = []
    for indexDay, day in enumerate(days):
        timerange = timeranges[indexDay]
        for indexTime, time in enumerate(timerange):
            startTime = "07:30:00"
            sumOfTimeUntilThisIndex = sum(timerange[:indexTime])
            duration =  timedelta(hours=sumOfTimeUntilThisIndex)
            slot = "%s - %s" %(day, sumTime(startTime, duration))
            timeslots.append(str(slot))
    return timeslots    

timeslots = generateTimeslots()

#Generate csv file 
targetFilePath = Path("main/resources/dataset/processed", "timeslots.csv")
targetFile = open(targetFilePath, 'w', newline="")

# write the file
writer = csv.writer(targetFile, dialect="excel", delimiter=";")
writer.writerow(["timeslots"])
i = 0
while i!=len(timeslots):
    writer.writerow(timeslots[i:i+1])
    i+=1
targetFile.close()

# replace quotation marks
with open(targetFilePath, "r+", encoding="utf-8") as csv_file:
    content = csv_file.read()

with open(targetFilePath, "w+", encoding="utf-8") as csv_file:
    csv_file.write(content.replace('"', ''))