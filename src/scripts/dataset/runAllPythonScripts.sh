#!/bin/bash

# execute all files with .py extension in this folder
find . -maxdepth 1 -name \*.py -exec python3 {} \;

# copy files generated to maven resources
# because maven only packs resources in src/main/resources in the jar file
. ../copyAllDatasetsToMavenResourcesFolder.sh