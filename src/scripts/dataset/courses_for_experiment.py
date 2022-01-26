import os
from pathlib import Path
import pandas as pd
import csv

# get project root, relative to this folder: if the folder changes, the path MUST be changed as well.
PROJECT_ROOT = Path().resolve().parent.parent

# initialize the directory
os.chdir(PROJECT_ROOT)
coursesFilePath = Path('main/resources/dataset/processed', 'courses.csv')

# read courses.csv file
courseFile = pd.read_csv(coursesFilePath, sep=";")
courses = courseFile["courses"].to_list()

# function to create new dataset
def createNewDatasetOnGivenLength(dName, dLength):
    datasetLength = 13 if dLength is None else dLength
    datasetName = "small" if dName is None else dName

    targetFilePath = Path("main/resources/dataset/processed", "courses_"+datasetName+".csv")
    targetFile = open(targetFilePath, 'w', newline="")
    writer = csv.writer(targetFile, dialect="excel", delimiter=";")
    writer.writerow(["courses", "weekly_hours", "instructors", "group"])
    i = 0
    while i != datasetLength:
        writer.writerow([courseFile['courses'].to_list()[i:i+1], courseFile['weekly_hours'].to_list()[i:i+1],
                        courseFile['instructors'].to_list()[i:i+1], courseFile['group'].to_list()[i:i+1]])
        i += 1
    targetFile.close()

    cleanDataset(targetFilePath)

def cleanDataset(targetFilePath):
    with open(targetFilePath, "r+", encoding="utf-8") as csv_file:
        content = csv_file.read()

    with open(targetFilePath, "w+", encoding="utf-8") as csv_file:
        newContent = content.replace('"', '').replace(
            "[", "").replace("]", "").replace("\'", "").replace(", ", ",")
        csv_file.write(newContent)

# produce dataset
# limit dataset on specific lengths for experiment purposes
# small : [13 courses]
# medium : [52 courses]
# large : [104 courses]
createNewDatasetOnGivenLength("small", 13)
createNewDatasetOnGivenLength("medium", 52)
createNewDatasetOnGivenLength("large", 104)







