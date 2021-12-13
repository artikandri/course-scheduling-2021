#!/bin/bash

# compile and run java file from given argument 
# argument: {script_name}.sh -fp java/file/value -cp /classpath/value
# ex path value: com/course_scheduling/assets/DatasetProcessor.java
# ex classpath value: ../../../target/dependency/{jar_name}.jar
# separate multiple jars with ":"
# dependencies: java, javac

# 12/12/2021: failed to flag arguments, so for the time being uses $1, $2... to locate arguments
# $2 refers to filepath value, $4 to classpath value
# to do: add flags: -fp filepath, -cp classpath

filepath=$2
classpath=$4
javaPackage=$(echo "$filepath" | sed "s/\//./g" | sed "s/.java//g")
javac $filepath -cp $classpath
java $javaPackage -cp $classpath