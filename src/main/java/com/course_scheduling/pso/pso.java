package com.course_scheduling.pso;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

/**
 *
 * @author Teddy Ferdinan
 */
import java.util.Arrays;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class pso {

    static class Configuration {
        int countParticles;
        int currentIteration;
        String psoMode;
        int maxIteration = 1;
        int targetFV = 1;
        int globalBestPosition;
        double globalBestFV;
        double[][][] positions;
        double[] velocities;
        double[] fitnessValues;

        String[] possibleRooms;
        String[] possibleCourses;
        String[] possibleTimes;
        float[] possibleTimesDurations;
        String[] coursesGroups;
        float[] coursesWeeklyHours;
        String[] coursesPreferredTimes;
        String[] courseInstructorPairs;

        String[][] allCourses;
        int timeslotsMon = 0;
        int timeslotsTue = 0;
        int timeslotsWed = 0;
        int timeslotsThu = 0;
        int timeslotsFri = 0;

        Configuration(String psoMode, int maxLimitMode, int countParticles, int currentIteration, int globalBestPosition, double globalBestFV, double[][][] positions, double[] velocities, String[] possibleCourses, String[] possibleTimes, float[] possibleTimesDurations, String[] coursesGroups, float[] coursesWeeklyHours, String[] coursesPreferredTimes, String[] courseInstructorPairs, String[][] allCourses, String[] possibleRooms) {
            this.countParticles = countParticles;
            this.currentIteration = currentIteration;
            this.psoMode = psoMode;
            if(psoMode == "limitFV") {
                this.targetFV = maxLimitMode;
            } else {
                this.maxIteration = maxLimitMode;
            }
            this.globalBestPosition = globalBestPosition;
            this.globalBestFV = globalBestFV;
            this.positions = positions;
            this.velocities = velocities;
            this.fitnessValues = new double[countParticles];

            this.possibleCourses = possibleCourses;
            this.possibleTimes = possibleTimes;
            this.possibleTimesDurations = possibleTimesDurations;
            this.coursesGroups = coursesGroups;
            this.coursesWeeklyHours = coursesWeeklyHours;
            this.coursesPreferredTimes = coursesPreferredTimes;
            this.courseInstructorPairs = courseInstructorPairs;
            this.possibleRooms = possibleRooms;

            this.allCourses = allCourses;
            for(int timeslot=0; timeslot < possibleTimes.length; timeslot++) {
                //count how many timeslots available in each day
                if(possibleTimes[timeslot].contains("Monday")) {
                    this.timeslotsMon++;//9, e.g. 0 - 8
                } else if(possibleTimes[timeslot].contains("Tuesday")) {
                    this.timeslotsTue++;//8, e.g. 9 - 16
                } else if(possibleTimes[timeslot].contains("Wednesday")) {
                    this.timeslotsWed++;//11, e.g. 17 - 27
                } else if(possibleTimes[timeslot].contains("Thursday")) {
                    this.timeslotsThu++;//10, e.g. 28 - 37
                } else {
                    this.timeslotsFri++;//11, e.g. 38 - 48
                }
            }
        }

        String show() {
            String output = "";
            output = output + "--------CONFIGURATION---------\n";
            output = output + "Number of Particles       : " + countParticles + "\n";
            output = output + "PSO Running Mode          : " + psoMode + "\n";
            output = output + "Maximum Iteration         : " + maxIteration + "\n";
            output = output + "Target FV                 : " + targetFV + "\n";
            output = output + "Global Best Position      : " + globalBestPosition + "\n";
            output = output + "Global Best Fitness Value : " + (1/globalBestFV) + "\n";
            for(int i=0; i < positions.length; i++) {
                output = output + "Particle " + i + ": ";
                for(int j=0; j < positions[i].length; j++) {
                    output = output + Arrays.toString(positions[i][j]) + ",";
                }
                output = output + "| FV: " + fitnessValues[i] + "\n";
            }
            return output;
        }
    }

    public static String[][] psoAllCourses(String[] possibleCourses, String[] coursesGroups) {
        //all courses, split courses into groups 0 - 49 here
        int allCoursesLength = possibleCourses.length/13;
        String[][] allCourses = new String[allCoursesLength][13];
        int groupCourseIndex = 0;
        for(int j=0; j < possibleCourses.length; j++) {
            if(groupCourseIndex == 13) groupCourseIndex = 0;
            String groupCode = coursesGroups[j];
            int groupCodeInt = Integer.parseInt(groupCode);
            allCourses[groupCodeInt][groupCourseIndex] = Integer.toString(j);
            groupCourseIndex++;
        }
        return allCourses;
    }

    private static Configuration psoInitialize(String psoMode, int maxLimitMode, int countParticles, String[] possibleRooms, String[] possibleCourses, String[] possibleTimes, float[] possibleTimesDurations, String[] coursesGroups, float[] coursesWeeklyHours, String[] coursesPreferredTimes, String[] courseInstructorPairs) {
        int currentIteration = 0;
        int globalBestPosition = 0;
        double globalBestFV = 0;
        Random r = new Random();

        int particleDimensionLength = possibleTimes.length * (int)Math.floor(possibleCourses.length/13);//49 * 50 = 2450
        double[][][] positions = new double[countParticles][particleDimensionLength][2];//positions at x (room), y (course)
        double[] velocities = new double[countParticles];

        //all rooms
        ArrayList<String> remainingRooms = new ArrayList<String>();
        for(int j=0; j < possibleRooms.length; j++) {
            remainingRooms.add(Integer.toString(j));
        }
        ArrayList<String> allRooms = new ArrayList<String>(remainingRooms);
        //all courses, split courses into groups 0 - 49 here
        String[][] allCourses = psoAllCourses(possibleCourses, coursesGroups);

        //initialize room, course, timeslot randomly in two steps
        ArrayList<String> remainingCourses = new ArrayList<String>();
        ArrayList<String> remainingTimes = new ArrayList<String>();
        ArrayList<String> remainingTimes1h = new ArrayList<String>();
        ArrayList<String> remainingTimes15h = new ArrayList<String>();
        ArrayList<String> remainingRoomsBooked = new ArrayList<String>();
        double assignedRoom, assignedCourse;
        int xIndex, yIndex, zIndex, assignedTime;
        String xValue, yValue, zValue;
        float assignedCourseWeeklyLoad;
        //first step - assign courses to timeslots
        for(int i=0; i < countParticles; i++) {
            for(int k=0; k < allCourses.length; k++) {
                //build course pool
                remainingCourses.clear();
                for(int x=0; x < allCourses[k].length; x++) {
                    remainingCourses.add(allCourses[k][x]);
                }
                //build timeslot pool
                remainingTimes.clear();
                remainingTimes1h.clear();
                remainingTimes15h.clear();
                int tsmin = k * possibleTimes.length;
                int tsmax = ((k+1) * possibleTimes.length);
                int realTimeslot = 0;
                for(int x=tsmin; x < tsmax; x++) {
                    remainingTimes.add(Integer.toString(x));
                    //consider each timeslot's duration
                    if(realTimeslot == possibleTimes.length) realTimeslot = 0;
                    if(possibleTimesDurations[realTimeslot] < 1.49) {
                        //1h slot
                        remainingTimes1h.add(Integer.toString(x));
                    } else {
                        //1.5h slot
                        remainingTimes15h.add(Integer.toString(x));
                    }
                    realTimeslot++;
                }
                //spare eight 1.5h slots to allow swapping
                int spareCounter = 8;
                while(spareCounter > 0) {
                    int indexToBeSpared = remainingTimes15h.size() - 1;
                    remainingTimes15h.remove(indexToBeSpared);
                    spareCounter--;
                }
                //loop the remaining courses
                int initialCoursePoolSize = remainingCourses.size();
                for(int j=0; j < initialCoursePoolSize; j++) {
                    //choose a course and remove selection from pool
                    yIndex = r.nextInt(remainingCourses.size());
                    yValue = remainingCourses.get(yIndex);
                    remainingCourses.remove(yIndex);
                    assignedCourse = Integer.parseInt(yValue);
                    //check the courses' weekly load
                    assignedCourseWeeklyLoad = coursesWeeklyHours[(int)assignedCourse];
                    //if 1h course
                    if(assignedCourseWeeklyLoad > 0.5 && assignedCourseWeeklyLoad < 1.5) {
                        //assign a timeslot from 1h pool and remove selection from the pool
                        if(remainingTimes1h.size() > 0) {
                            zIndex = 0;
                            zValue = remainingTimes1h.get(zIndex);
                            remainingTimes1h.remove(zIndex);
                            assignedTime = Integer.parseInt(zValue);
                            if(assignedCourse == 0) assignedCourse = 0.47;
                            positions[i][assignedTime][1] = assignedCourse;
                        } else {
                            //if there is no more 1h slot left
                            zIndex = 0;
                            zValue = remainingTimes15h.get(zIndex);
                            remainingTimes15h.remove(zIndex);
                            assignedTime = Integer.parseInt(zValue);
                            if(assignedCourse == 0) assignedCourse = 0.47;
                            positions[i][assignedTime][1] = assignedCourse;
                        }
                    }
                    //if 2h course
                    if(assignedCourseWeeklyLoad > 1.5 && assignedCourseWeeklyLoad < 2.5) {
                        //assign two timeslots from 1h pool and remove selections from the pool
                        if(remainingTimes1h.size() > 0) {
                            for(int assignmentRepeater=0; assignmentRepeater < 2; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes1h.get(zIndex);
                                remainingTimes1h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                        } else {
                            //if there is no more 1h slot left
                            for(int assignmentRepeater=0; assignmentRepeater < 2; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes15h.get(zIndex);
                                remainingTimes15h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                        }
                    }
                    //if 3h course
                    if(assignedCourseWeeklyLoad > 2.5 && assignedCourseWeeklyLoad < 3.5) {
                        //try to assign to 1.5h pool first if there are enough timeslots
                        if(remainingTimes15h.size() >= 2) {
                            //assign two timeslots from 1.5h pool and remove selections from the pool
                            for(int assignmentRepeater=0; assignmentRepeater < 2; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes15h.get(zIndex);
                                remainingTimes15h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                        } else if(remainingTimes1h.size() >= 3) {
                            //assign three timeslots from 1h pool and remove selections from the pool
                            for(int assignmentRepeater=0; assignmentRepeater < 3; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes1h.get(zIndex);
                                remainingTimes1h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                        } else if(remainingTimes1h.size() >=2) {
                            //assign two timeslots from 1h pool and one timeslot from 1.5h pool
                            for(int assignmentRepeater=0; assignmentRepeater < 2; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes1h.get(zIndex);
                                remainingTimes1h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                            zIndex = 0;
                            zValue = remainingTimes15h.get(zIndex);
                            remainingTimes15h.remove(zIndex);
                            assignedTime = Integer.parseInt(zValue);
                            if(assignedCourse == 0) assignedCourse = 0.47;
                            positions[i][assignedTime][1] = assignedCourse;
                        } else {
                            //assign one timeslot from 1h pool and two timeslots from 1.5h pool
                            for(int assignmentRepeater=0; assignmentRepeater < 2; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes15h.get(zIndex);
                                remainingTimes15h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                            zIndex = 0;
                            zValue = remainingTimes1h.get(zIndex);
                            remainingTimes1h.remove(zIndex);
                            assignedTime = Integer.parseInt(zValue);
                            if(assignedCourse == 0) assignedCourse = 0.47;
                            positions[i][assignedTime][1] = assignedCourse;
                        }
                    }
                    //if 4h course
                    if(assignedCourseWeeklyLoad > 3.5) {
                        //try to assign to 1.5h pool first if there are enough timeslots
                        if(remainingTimes15h.size() >= 2) {
                            //assign two timeslots from 1.5h pool and remove selections from the pool
                            for(int assignmentRepeater=0; assignmentRepeater < 2; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes15h.get(zIndex);
                                remainingTimes15h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                            //assign a timeslot from 1h pool and remove selection from the pool
                            zIndex = 0;
                            zValue = remainingTimes1h.get(zIndex);
                            remainingTimes1h.remove(zIndex);
                            assignedTime = Integer.parseInt(zValue);
                            if(assignedCourse == 0) assignedCourse = 0.47;
                            positions[i][assignedTime][1] = assignedCourse;
                        } else if(remainingTimes1h.size() >= 4) {
                            //assign four timeslots from 1h pool and remove selections from the pool
                            for(int assignmentRepeater=0; assignmentRepeater < 4; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes1h.get(zIndex);
                                remainingTimes1h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                        } else if(remainingTimes1h.size() >=3) {
                            //assign three timeslots from 1h pool and one timeslot from 1.5h pool
                            for(int assignmentRepeater=0; assignmentRepeater < 3; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes1h.get(zIndex);
                                remainingTimes1h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                            zIndex = 0;
                            zValue = remainingTimes15h.get(zIndex);
                            remainingTimes15h.remove(zIndex);
                            assignedTime = Integer.parseInt(zValue);
                            if(assignedCourse == 0) assignedCourse = 0.47;
                            positions[i][assignedTime][1] = assignedCourse;
                        } else if(remainingTimes1h.size() >=2) {
                            //assign two timeslots from 1h pool and two timeslots from 1.5h pool
                            for(int assignmentRepeater=0; assignmentRepeater < 2; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes1h.get(zIndex);
                                remainingTimes1h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                            for(int assignmentRepeater=0; assignmentRepeater < 2; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes15h.get(zIndex);
                                remainingTimes15h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                        } else {
                            //assign one timeslot from 1h pool and three timeslots from 1.5h pool
                            for(int assignmentRepeater=0; assignmentRepeater < 3; assignmentRepeater++) {
                                zIndex = 0;
                                zValue = remainingTimes15h.get(zIndex);
                                remainingTimes15h.remove(zIndex);
                                assignedTime = Integer.parseInt(zValue);
                                if(assignedCourse == 0) assignedCourse = 0.47;
                                positions[i][assignedTime][1] = assignedCourse;
                            }
                            zIndex = 0;
                            zValue = remainingTimes1h.get(zIndex);
                            remainingTimes1h.remove(zIndex);
                            assignedTime = Integer.parseInt(zValue);
                            if(assignedCourse == 0) assignedCourse = 0.47;
                            positions[i][assignedTime][1] = assignedCourse;
                        }
                    }
                }
            }
        }
        //second step - assign a room if a course exists in a timeslot
        for(int i=0; i < countParticles; i++) {
            for(int j=0; j < possibleTimes.length; j++) {
                //build room pool
                remainingRooms.clear();
                remainingRooms.addAll(allRooms);
                remainingRoomsBooked.clear();
                //check for 'booked' rooms by continuous courses
                for(int k=0; k < allCourses.length; k++) {
                    int timeslotIndex = j+(k*possibleTimes.length);
                    if(positions[i][timeslotIndex][1] != 0) {
                        if(timeslotIndex > 0) {
                            if(positions[i][timeslotIndex][1] == positions[i][timeslotIndex-1][1]) {
                                String theBookedRoom = Integer.toString((int)positions[i][timeslotIndex-1][0]);
                                remainingRoomsBooked.add(theBookedRoom);
                                int theBookedRoomIndex = remainingRooms.indexOf(theBookedRoom);
                                remainingRooms.remove(theBookedRoomIndex);
                            }
                        }
                    }
                }
                //loop across the schedules
                for(int k=0; k < allCourses.length; k++) {
                    int timeslotIndex = j+(k*possibleTimes.length);
                    if(positions[i][timeslotIndex][1] != 0) {
                        if(timeslotIndex > 0) {
                            if(positions[i][timeslotIndex][1] == positions[i][timeslotIndex-1][1]) {
                                //if the course is continuous, assign from 'booked' pool
                                xValue = Integer.toString((int)positions[i][timeslotIndex-1][0]);
                                xIndex = remainingRoomsBooked.indexOf(xValue);
                                remainingRoomsBooked.remove(xIndex);
                                assignedRoom = Integer.parseInt(xValue);
                            } else {
                                //assign a room from unbooked pool
                                xIndex = r.nextInt(remainingRooms.size());
                                xValue = remainingRooms.get(xIndex);
                                remainingRooms.remove(xIndex);
                                assignedRoom = Integer.parseInt(xValue);
                            }
                        } else {
                            //assign a room from unbooked pool
                            xIndex = r.nextInt(remainingRooms.size());
                            xValue = remainingRooms.get(xIndex);
                            remainingRooms.remove(xIndex);
                            assignedRoom = Integer.parseInt(xValue);
                        }
                        //put the assignment into the slot in the particle
                        if(assignedRoom == 0) assignedRoom = 0.47;
                        positions[i][timeslotIndex][0] = assignedRoom;
                    }
                }
            }
        }

        //initialize velocities randomly
        for(int i=0; i < countParticles; i++) {
            velocities[i] = r.nextDouble();
        }

        return new Configuration(psoMode, maxLimitMode, countParticles, currentIteration, globalBestPosition, globalBestFV, positions, velocities, possibleCourses, possibleTimes, possibleTimesDurations, coursesGroups, coursesWeeklyHours, coursesPreferredTimes, courseInstructorPairs, allCourses, possibleRooms);
    }

    private static Configuration psoFindGlobalBest(Configuration x) {
        int particleCount = x.positions.length;
        int particleLength = x.positions[0].length;        
        Double[] fitnessValues = new Double[particleCount];
        Double[] penalties = new Double[particleCount];
        ArrayList<String> values = new ArrayList<String>();

        //all courses, split courses into groups 0 - 49 here
        String[][] allCourses = x.allCourses;

        //initial penalty values
        for(int i=0; i < particleCount; i++) {
            penalties[i] = 0.0;
        }

        //check constraint #1
        //Courses taught by the same instructor can't be held simultaneously. [violation => penalty +99]
        //do the check across the same timeslot, so split one particle into its schedules
        for(int i=0; i < particleCount; i++) {
            for(int j=0; j < x.possibleTimes.length; j++) {
                values.clear();
                for(int k=0; k < allCourses.length; k++) {
                    if(x.positions[i][j+(k*x.possibleTimes.length)][1] != 0) {
                        double course = x.positions[i][j+(k*x.possibleTimes.length)][1];
                        String value = x.courseInstructorPairs[(int)course];
                        values.add(value);
                    }
                }
                for(int k=0; k < allCourses.length; k++) {
                    if(x.positions[i][j+(k*x.possibleTimes.length)][1] != 0) {
                        double course = x.positions[i][j+(k*x.possibleTimes.length)][1];
                        String value = x.courseInstructorPairs[(int)course];
                        int frequency = Collections.frequency(values, value);
                        if(frequency > 1) penalties[i] = penalties[i] + (frequency * 9999);
                    }
                }
            }
        }

        //check constraint #2
        //The same room can’t be scheduled for 2 different courses simultaneously. [violation => penalty +99]
        //do the check across the same timeslot, so split one particle into its schedules
        for(int i=0; i < particleCount; i++) {
            for(int j=0; j < x.possibleTimes.length; j++) {
                values.clear();
                for(int k=0; k < allCourses.length; k++) {
                    if(x.positions[i][j+(k*x.possibleTimes.length)][0] != 0) {
                        double room = x.positions[i][j+(k*x.possibleTimes.length)][0];
                        String value = x.possibleRooms[(int)room];
                        values.add(value);
                    }
                }
                for(int k=0; k < allCourses.length; k++) {
                    if(x.positions[i][j+(k*x.possibleTimes.length)][0] != 0) {
                        double room = x.positions[i][j+(k*x.possibleTimes.length)][0];
                        String value = x.possibleRooms[(int)room];
                        int frequency = Collections.frequency(values, value);
                        if(frequency > 1) penalties[i] = penalties[i] + (frequency * 9999);
                    }
                }
            }
        }

        //check constraint #3
        //Maintain preference of instructor as much as possible. [violation => penalty +1]
        //do the check across the same timeslot, so split one particle into its schedules
        for(int i=0; i < x.possibleCourses.length; i++) {
            String instructorCode = x.courseInstructorPairs[i];
            String instructorPreferences = x.coursesPreferredTimes[Integer.parseInt(instructorCode)].replaceAll(" ", "");
            if(instructorPreferences != "" && !(instructorPreferences.isEmpty())) {
                String[] instructorPreferencesArray = instructorPreferences.split(",");
                for(int j=0; j < instructorPreferencesArray.length; j++) {
                    if(instructorPreferencesArray[j] != "" && !(instructorPreferencesArray[j].isEmpty())) {
                        int tsPreferred = Integer.parseInt(instructorPreferencesArray[j]);
                        for(int k=0; k < particleCount; k++) {
                            for(int z=0; z < allCourses.length; z++) {
                                if(x.positions[k][tsPreferred+(z*x.possibleTimes.length)][1] != i) {
                                    penalties[k] = penalties[k] + 1;
                                }
                            }
                        }
                    }
                }
            }
        }

        //check constraint #4
        //Geometrical shapes of 2h, 3h, 4h, and 5h courses must be considered. [violation => penalty +2]
        //do the check per schedule in each particle
        for(int particle=0; particle < particleCount; particle++) {
            int timeslot = 0;
            for(int particleTimeslot=0; particleTimeslot < particleLength; particleTimeslot++) {
                if(timeslot == x.possibleTimes.length) {
                    timeslot = 0;
                }
                double courseIndex = x.positions[particle][particleTimeslot][1];
                if(courseIndex != 0.0) {
                    float courseLoad = x.coursesWeeklyHours[(int)courseIndex];
                    if(courseLoad > 1) {
                        //this evaluation approach works for all 2h, 3h, and 4h courses!
                        //the reason: a 2h course should not be split,
                        //and this approach already checks for such a consecutive timeslot pair
                        //a 3h course is ideally split into two 1.5h slots, and again
                        //this approach already checks for such a consecutive timeslot pair
                        //alternatively, a 3h course can be split into 2+1 or 1+2 and the penalty will be 2
                        //worst case is a 3h course is split into 1+1+1 and the penalty will be 6
                        //a 4h course is ideally split into 2+2, and this approach wll check
                        //each pair without causing a penalty
                        //alternatively, it will be split into 3+1 or 1+3 and the penalty will be 2
                        //worst case is it will be split into 1+1+1+1 and the penalty will be 8
                        int checkMarker = 0;
                        int neighbour1 = particleTimeslot-1;
                        int neighbour2 = particleTimeslot+1;
                        if(checkMarker == 0 && neighbour1 > -1) {
                            double neighbour1CourseIndex = x.positions[particle][neighbour1][1];
                            if(courseIndex == neighbour1CourseIndex) {
                                checkMarker = 1;
                            }
                        }
                        if(checkMarker == 0 && neighbour2 < particleLength) {
                            double neighbour2CourseIndex = x.positions[particle][neighbour2][1];
                            if(courseIndex == neighbour2CourseIndex) {
                                checkMarker = 1;
                            }
                        }
                        if(checkMarker == 0) penalties[particle] = penalties[particle] + 2;
                    }
                }
            }
        }

        //check constraint #5
        //Number of daily lessons for students should be evenly balanced. [violation => penalty +1]
        //do the check per schedule in each particle
        for(int particle=0; particle < particleCount; particle++) {
            //count how many classes in each day
            int timeslot = 0;
            int classesMon = 0;
            int classesTue = 0;
            int classesWed = 0;
            int classesThu = 0;
            int classesFri = 0;
            for(int particleTimeslot=0; particleTimeslot < particleLength; particleTimeslot++) {
                if(timeslot == (x.possibleTimes.length-1)) {
                    //check if in one day there are more than 5 classes
                    if(classesMon > 5) penalties[particle] = penalties[particle] + (99 * (classesMon-5));
                    if(classesTue > 5) penalties[particle] = penalties[particle] + (99 * (classesTue-5));
                    if(classesWed > 5) penalties[particle] = penalties[particle] + (99 * (classesWed-5));
                    if(classesThu > 5) penalties[particle] = penalties[particle] + (99 * (classesThu-5));
                    if(classesFri > 5) penalties[particle] = penalties[particle] + (99 * (classesFri-5));
                    //compare the number of classes among the days, tolerance is +/- 1
                    if(Math.sqrt(classesMon-classesTue) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesMon-classesTue);
                    if(Math.sqrt(classesMon-classesWed) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesMon-classesWed);
                    if(Math.sqrt(classesMon-classesThu) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesMon-classesThu);
                    if(Math.sqrt(classesMon-classesFri) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesMon-classesFri);
                    if(Math.sqrt(classesTue-classesWed) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesTue-classesWed);
                    if(Math.sqrt(classesTue-classesThu) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesTue-classesThu);
                    if(Math.sqrt(classesTue-classesFri) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesTue-classesFri);
                    if(Math.sqrt(classesWed-classesThu) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesWed-classesThu);
                    if(Math.sqrt(classesWed-classesFri) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesWed-classesFri);
                    if(Math.sqrt(classesThu-classesFri) > 1) penalties[particle] = penalties[particle] + Math.sqrt(classesThu-classesFri);
                    //reset the counters
                    timeslot = 0;
                    classesMon = 0;
                    classesTue = 0;
                    classesWed = 0;
                    classesThu = 0;
                    classesFri = 0;
                }
                if(x.positions[particle][particleTimeslot][1] != 0) {
                    if(particleTimeslot < x.timeslotsMon) {
                        classesMon++;
                    } else if(x.timeslotsMon >= particleTimeslot && particleTimeslot < (x.timeslotsMon + x.timeslotsTue)) {
                        classesTue++;
                    } else if((x.timeslotsMon + x.timeslotsTue) >= particleTimeslot && particleTimeslot < (x.timeslotsMon + x.timeslotsTue + x.timeslotsWed)) {
                        classesWed++;
                    } else if((x.timeslotsMon + x.timeslotsTue + x.timeslotsWed) >= particleTimeslot && particleTimeslot < (x.timeslotsMon + x.timeslotsTue + x.timeslotsWed + x.timeslotsThu)) {
                        classesThu++;
                    } else {
                        classesFri++;
                    }
                }
                timeslot++;
            }
        }

        //find index with the lowest penalty score
        double globalBestPenalty = penalties[0];
        int globalBestIndex = 0;
        for(int i=0; i < penalties.length; i++) {
            x.fitnessValues[i] = 1/penalties[i];
            if(globalBestPenalty > penalties[i]) {
                globalBestPenalty = penalties[i];
                globalBestIndex = i;
            }
        }
        x.globalBestFV = globalBestPenalty;
        x.globalBestPosition = globalBestIndex;

        return x;
    }

    private static Configuration psoMovement(Configuration x) {
        Random r = new Random();
        int globalBestIndex = x.globalBestPosition;

        for(int particle=0; particle < x.countParticles; particle++) {
            if(particle != globalBestIndex) {
                double[][] timeslotsMarks = new double[x.positions[particle].length][2];
                for(int i=0; i < x.positions[particle].length; i++) {
                    if(x.positions[particle][i][1] == 0) {
                        //empty timeslot, marked as available
                        timeslotsMarks[i][1] = -99;
                    } else if(x.positions[particle][i][1] != x.positions[globalBestIndex][i][1]) {
                        //only mark the course for movement if it's not in the same timeslot as the global best
                        timeslotsMarks[i][0] = x.positions[particle][i][1];//what course
                        timeslotsMarks[i][1] = 1;//marked for movement
                    }
                }

                //course re-assignment mechanism
                int groupCounter = 0;
                int timeslotCounter = 0;
                int emptyTimeslotsMin = -49;//inclusive
                int emptyTimeslotsMax = 0;//exclusive
                ArrayList<Integer> emptyTimeslots1h = new ArrayList<Integer>();
                ArrayList<Integer> emptyTimeslots15h = new ArrayList<Integer>();
                int[] coursePairIndex = new int[3];
                for(int i=0; i < x.positions[particle].length; i++) {
                    if(timeslotCounter == x.possibleTimes.length) {
                        timeslotCounter = 0;
                    }
                    if(timeslotCounter == 0) {
                        groupCounter++;
                        if(groupCounter <= x.possibleCourses.length/13) {
                            emptyTimeslotsMin = emptyTimeslotsMin + x.possibleTimes.length;
                            emptyTimeslotsMax = emptyTimeslotsMax + x.possibleTimes.length;
                            //build the empty timeslot pool
                            emptyTimeslots1h.clear();
                            emptyTimeslots15h.clear();
                            if(emptyTimeslotsMin < x.positions[particle].length) {
                                for(int j=emptyTimeslotsMin; j < emptyTimeslotsMax; j++) {
                                    if(timeslotsMarks[j][1] == -99) {
                                        int timeslotDurationIndex = j - (x.possibleTimesDurations.length * (groupCounter-1));
                                        if(x.possibleTimesDurations[timeslotDurationIndex] < 1.49) {
                                            emptyTimeslots1h.add(j);
                                        } else {
                                            emptyTimeslots15h.add(j);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(timeslotsMarks[i][1] == 1 && x.positions[particle][i][1] != 0) {
                        //timeslotsMarks[i][1] == 1 => checks if the slot is marked for re-assignment
                        //x.positions[particle][i][1] != 0 => checks if the slot has actually been moved by prev iter
                        coursePairIndex[0] = 0;//for storing the course's actual index (from the dataset)
                        coursePairIndex[1] = -1;//for storing its timeslot index
                        coursePairIndex[2] = -1;//for storing its next timeslot index if consecutive
                        //now, we will re-assign the course to an empty timeslot
                        //but, first, check if the course is consecutive
                        int timeslotDurationIndex = i - (x.possibleTimesDurations.length * (groupCounter-1));
                        int currentCourseIndex = (int)x.positions[particle][i][1];
                        int timeslotNeighbour = timeslotDurationIndex+1;
                        if(timeslotNeighbour < x.positions[particle].length && timeslotNeighbour < x.possibleTimes.length) {
                            int neighbourCourseIndex = (int)x.positions[particle][i+1][1];
                            if(currentCourseIndex == neighbourCourseIndex && 
                            x.possibleTimesDurations[timeslotDurationIndex] == x.possibleTimesDurations[timeslotNeighbour]) {
                                coursePairIndex[0] = currentCourseIndex;
                                coursePairIndex[1] = i;
                                coursePairIndex[2] = timeslotNeighbour;
                            }
                        }
                        //now, the actual course re-assignment takes place
                        float currentCourseSlotDuration = x.possibleTimesDurations[timeslotDurationIndex];
                        if(currentCourseSlotDuration < 1.49) {
                            //if it is a singular non-consecutive course, re-assign to an empty timeslot of same duration
                            if(coursePairIndex[1] != -1 && coursePairIndex[2] == -1 && emptyTimeslots1h.size()!=0) {
                                int zIndex = r.nextInt(emptyTimeslots1h.size());
                                int zValue = emptyTimeslots1h.get(zIndex);
                                x.positions[particle][zValue][1] = coursePairIndex[0];
                                x.positions[particle][coursePairIndex[1]][1] = 0;
                                emptyTimeslots1h.remove(zIndex);
                                //emptyTimeslots1h.add(coursePairIndex[1]);
                            } else if(coursePairIndex[1] != -1 && coursePairIndex[2] != -1 && emptyTimeslots1h.size()!=0) {
                                //if it is a consecutive course, re-assign both as long as there are enough empty slots
                                if(emptyTimeslots1h.size() > 2) {
                                    int zMarker = 0;
                                    int zRepeatCounter = 3;
                                    int zIndex1 = -47;
                                    int zIndex2 = -47;
                                    int zValue1 = -47;
                                    int zValue2 = -47;
                                    //try to find two consecutive empty slots up to 3X
                                    while(zMarker == 0 && zRepeatCounter > 0) {
                                        zIndex1 = r.nextInt(emptyTimeslots1h.size());
                                        zValue1 = emptyTimeslots1h.get(zIndex1);
                                        if(emptyTimeslots1h.indexOf((zValue1+1)) != -1) {
                                            zValue2 = (zValue1+1);
                                            zMarker = 1;
                                        } else if(emptyTimeslots1h.indexOf((zValue1-1)) != -1) {
                                            zValue2 = (zValue1-1);
                                            zMarker = 1;
                                        }
                                        zRepeatCounter--;
                                    }
                                    //only re-assign if the two empty slots are consecutive
                                    if(zMarker == 1) {
                                        x.positions[particle][zValue1][1] = coursePairIndex[0];
                                        x.positions[particle][zValue2][1] = coursePairIndex[0];
                                        x.positions[particle][coursePairIndex[1]][1] = 0;
                                        x.positions[particle][coursePairIndex[2]][1] = 0;
                                        emptyTimeslots1h.remove(zIndex1);
                                        zIndex2 = emptyTimeslots1h.indexOf(zValue2);
                                        emptyTimeslots1h.remove(zIndex2);
                                        //emptyTimeslots1h.add(coursePairIndex[1]);
                                        //emptyTimeslots1h.add(coursePairIndex[2]);
                                    }
                                } else if(emptyTimeslots1h.size() == 2) {
                                    int zValue1 = emptyTimeslots1h.get(0);
                                    int zValue2 = emptyTimeslots1h.get(1);
                                    //only re-assign if the two empty slots are consecutive
                                    if(Math.sqrt(zValue1-zValue2) == 1) {
                                        x.positions[particle][zValue1][1] = coursePairIndex[0];
                                        x.positions[particle][zValue2][1] = coursePairIndex[0];
                                        x.positions[particle][coursePairIndex[1]][1] = 0;
                                        x.positions[particle][coursePairIndex[2]][1] = 0;
                                        emptyTimeslots1h.remove(0);//was 2 remaining, now only 1
                                        emptyTimeslots1h.remove(0);//was 1 remaining, now empty
                                        //emptyTimeslots1h.add(coursePairIndex[1]);
                                        //emptyTimeslots1h.add(coursePairIndex[2]);
                                    }
                                }
                            }
                        } else {
                            //if it is a singular non-consecutive course, re-assign to an empty timeslot of same duration
                            if(coursePairIndex[1] != -1 && coursePairIndex[2] == -1 && emptyTimeslots15h.size()!=0) {
                                int zIndex = r.nextInt(emptyTimeslots15h.size());
                                int zValue = emptyTimeslots15h.get(zIndex);
                                x.positions[particle][zValue][1] = coursePairIndex[0];
                                x.positions[particle][coursePairIndex[1]][1] = 0;
                                emptyTimeslots15h.remove(zIndex);
                                //emptyTimeslots15h.add(coursePairIndex[1]);
                            } else if(coursePairIndex[1] != -1 && coursePairIndex[2] != -1 && emptyTimeslots15h.size()!=0) {
                                //if it is a consecutive course, re-assign both as long as there are enough empty slots
                                if(emptyTimeslots15h.size() > 2) {
                                    int zMarker = 0;
                                    int zRepeatCounter = 3;
                                    int zIndex1 = -47;
                                    int zIndex2 = -47;
                                    int zValue1 = -47;
                                    int zValue2 = -47;
                                    //try to find two consecutive empty slots up to 3X
                                    while(zMarker == 0 && zRepeatCounter > 0) {
                                        zIndex1 = r.nextInt(emptyTimeslots15h.size());
                                        zValue1 = emptyTimeslots15h.get(zIndex1);
                                        if(emptyTimeslots15h.indexOf((zValue1+1)) != -1) {
                                            zValue2 = (zValue1+1);
                                            zMarker = 1;
                                        } else if(emptyTimeslots15h.indexOf((zValue1-1)) != -1) {
                                            zValue2 = (zValue1-1);
                                            zMarker = 1;
                                        }
                                        zRepeatCounter--;
                                    }
                                    //only re-assign if the two empty slots are consecutive
                                    if(zMarker == 1) {
                                        x.positions[particle][zValue1][1] = coursePairIndex[0];
                                        x.positions[particle][zValue2][1] = coursePairIndex[0];
                                        x.positions[particle][coursePairIndex[1]][1] = 0;
                                        x.positions[particle][coursePairIndex[2]][1] = 0;
                                        emptyTimeslots15h.remove(zIndex1);
                                        zIndex2 = emptyTimeslots15h.indexOf(zValue2);
                                        emptyTimeslots15h.remove(zIndex2);
                                        //emptyTimeslots15h.add(coursePairIndex[1]);
                                        //emptyTimeslots15h.add(coursePairIndex[2]);
                                    }
                                } else if(emptyTimeslots15h.size() == 2) {
                                    int zValue1 = emptyTimeslots15h.get(0);
                                    int zValue2 = emptyTimeslots15h.get(1);
                                    //only re-assign if the two empty slots are consecutive
                                    if(Math.sqrt(zValue1-zValue2) == 1) {
                                        x.positions[particle][zValue1][1] = coursePairIndex[0];
                                        x.positions[particle][zValue2][1] = coursePairIndex[0];
                                        x.positions[particle][coursePairIndex[1]][1] = 0;
                                        x.positions[particle][coursePairIndex[2]][1] = 0;
                                        emptyTimeslots15h.remove(0);//was 2 remaining, now only 1
                                        emptyTimeslots15h.remove(0);//was 1 remaining, now empty
                                        //emptyTimeslots15h.add(coursePairIndex[1]);
                                        //emptyTimeslots15h.add(coursePairIndex[2]);
                                    }
                                }
                            }
                        }
                    }
                    timeslotCounter++;
                }

                //re-assign rooms
                for(int roomReset=0; roomReset < x.positions[particle].length; roomReset++) {
                    x.positions[particle][roomReset][0] = 0;
                }
                ArrayList<String> remainingRooms = new ArrayList<String>();
                for(int j=0; j < x.possibleRooms.length; j++) {
                    remainingRooms.add(Integer.toString(j));
                }
                ArrayList<String> allRooms = new ArrayList<String>(remainingRooms);
                ArrayList<String> remainingRoomsBooked = new ArrayList<String>();
                double assignedRoom;
                int xIndex;
                String xValue;
                for(int j=0; j < x.possibleTimes.length; j++) {
                    //build room pool
                    remainingRooms.clear();
                    remainingRooms.addAll(allRooms);
                    remainingRoomsBooked.clear();
                    //check for 'booked' rooms by continuous courses
                    for(int k=0; k < x.allCourses.length; k++) {
                        int timeslotIndex = j+(k*(x.possibleTimes.length));
                        if(x.positions[particle][timeslotIndex][1] != 0) {
                            if(timeslotIndex > 0) {
                                if(x.positions[particle][timeslotIndex][1] == x.positions[particle][timeslotIndex-1][1]) {
                                    String theBookedRoom = Integer.toString((int)x.positions[particle][timeslotIndex-1][0]);
                                    remainingRoomsBooked.add(theBookedRoom);
                                    int theBookedRoomIndex = remainingRooms.indexOf(theBookedRoom);
                                    remainingRooms.remove(theBookedRoomIndex);
                                }
                            }
                        }
                    }
                    //loop across the schedules
                    for(int k=0; k < x.allCourses.length; k++) {
                        int timeslotIndex = j+(k*(x.possibleTimes.length));
                        if(x.positions[particle][timeslotIndex][1] != 0) {
                            if(timeslotIndex > 0) {
                                if(x.positions[particle][timeslotIndex][1] == x.positions[particle][timeslotIndex-1][1]) {
                                    //if the course is continuous, assign from 'booked' pool
                                    xValue = Integer.toString((int)x.positions[particle][timeslotIndex-1][0]);
                                    xIndex = remainingRoomsBooked.indexOf(xValue);
                                    remainingRoomsBooked.remove(xIndex);
                                    assignedRoom = Integer.parseInt(xValue);
                                } else {
                                    //assign a room from unbooked pool
                                    xIndex = r.nextInt(remainingRooms.size());
                                    xValue = remainingRooms.get(xIndex);
                                    remainingRooms.remove(xIndex);
                                    assignedRoom = Integer.parseInt(xValue);
                                }
                            } else {
                                //assign a room from unbooked pool
                                xIndex = r.nextInt(remainingRooms.size());
                                xValue = remainingRooms.get(xIndex);
                                remainingRooms.remove(xIndex);
                                assignedRoom = Integer.parseInt(xValue);
                            }
                            //put the assignment into the slot in the particle
                            if(assignedRoom == 0) assignedRoom = 0.47;
                            x.positions[particle][timeslotIndex][0] = assignedRoom;
                        }
                    }
                }

            }
        }
        return x;
    }

    private static Configuration psoIterate(Configuration x, String executionMode) {
        Configuration y = x;
        y = psoFindGlobalBest(y);
        if("limitFV".equals(y.psoMode)) {
            //iterate until global best FV is equal to or lower than target FV
            while(y.globalBestFV > y.targetFV) {
                //find global best
                y = psoFindGlobalBest(y);
                //particle movement
                y = psoMovement(y);
                y.currentIteration++;
                //print details if necessary
                if(executionMode == "detailed") {
                    System.out.printf("--Iteration %d: \n", y.currentIteration);
                    System.out.printf("%s", y.show());
                    System.out.printf("\n");
                }
            }
        } else {
            //iterate until maximum iteration limit is reached
            while(y.currentIteration < y.maxIteration) {
                //find global best
                y = psoFindGlobalBest(y);
                //particle movement
                y = psoMovement(y);
                y.currentIteration++;
                //print details if necessary
                if(executionMode == "detailed") {
                    System.out.printf("--Iteration %d: \n", y.currentIteration);
                    System.out.printf("%s", y.show());
                    System.out.printf("\n");
                }
            }
        }
        return y;
    }

    public static String psoMapSchedules(Configuration x) {
        String output = "-----Schedules, Global Best-----\n";
        int groupCounter = 1;
        int assignedTime = 0;
        for(int timeslot=0; timeslot < x.positions[x.globalBestPosition].length; timeslot++) {
            if(assignedTime == x.possibleTimes.length) assignedTime = 0;
            if(assignedTime == 0) {
                output += "--Group " + groupCounter + ":\n";
                groupCounter++;
            }
            String assignedTimeStr = x.possibleTimes[assignedTime];
            String assignedCourseStr = "-";
            String assignedRoomStr = "-";
            if(x.positions[x.globalBestPosition][timeslot][1] != 0) {
                int assignedCourse = (int)x.positions[x.globalBestPosition][timeslot][1];
                int assignedRoom = (int)x.positions[x.globalBestPosition][timeslot][0];
                assignedCourseStr = x.possibleCourses[assignedCourse];
                assignedRoomStr = x.possibleRooms[assignedRoom];
            }
            output += assignedTimeStr + " = " + assignedCourseStr + "(" + assignedRoomStr + ")\n";
            assignedTime++;
        }
        return output;
    }

    public static String[] psoGetPossibleRooms(String pathToFile) {
        DatasetProcessor reader = new DatasetProcessor();
        String room;
        String[] possibleRooms;
        String[] tempHolder = new String[3];

        List listRooms = reader.readFile(pathToFile);
        possibleRooms = new String[listRooms.size()-1];
        for(int i=1; i < listRooms.size(); i++) {
            room = listRooms.get(i).toString();
            possibleRooms[i-1] = room.replaceAll("\\[", "").replaceAll("\\]", "");
        }
        return possibleRooms;
    }

    public static String[] psoGetPossibleCourses(String pathToFile) {
        DatasetProcessor reader = new DatasetProcessor();
        String course;
        float[] coursesWeeklyHours;
        String[] possibleCourses, coursesGroups, courseInstructorPairs;
        String[] tempHolder = new String[3];

        List listCourses = reader.readFile(pathToFile);
        possibleCourses = new String[listCourses.size()-1];
        coursesWeeklyHours = new float[listCourses.size()-1];
        courseInstructorPairs = new String[listCourses.size()-1];
        coursesGroups = new String[listCourses.size()-1];
        for(int i=1; i < listCourses.size(); i++) {
            course = listCourses.get(i).toString();
            tempHolder = course.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("&amp;", "&").split(";", 4);
            possibleCourses[i-1] = tempHolder[0];
            coursesWeeklyHours[i-1] = Float.parseFloat(tempHolder[1]);
            courseInstructorPairs[i-1] = tempHolder[2];
            coursesGroups[i-1] = tempHolder[3];
        }

        return possibleCourses;
    }

    public static float[] psoGetCoursesWeeklyHours(String pathToFile) {
        DatasetProcessor reader = new DatasetProcessor();
        String course;
        float[] coursesWeeklyHours;
        String[] possibleCourses, coursesGroups, courseInstructorPairs;
        String[] tempHolder = new String[3];

        List listCourses = reader.readFile(pathToFile);
        possibleCourses = new String[listCourses.size()-1];
        coursesWeeklyHours = new float[listCourses.size()-1];
        courseInstructorPairs = new String[listCourses.size()-1];
        coursesGroups = new String[listCourses.size()-1];
        for(int i=1; i < listCourses.size(); i++) {
            course = listCourses.get(i).toString();
            tempHolder = course.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("&amp;", "&").split(";", 4);
            possibleCourses[i-1] = tempHolder[0];
            coursesWeeklyHours[i-1] = Float.parseFloat(tempHolder[1]);
            courseInstructorPairs[i-1] = tempHolder[2];
            coursesGroups[i-1] = tempHolder[3];
        }

        return coursesWeeklyHours;
    }

    public static String[] psoGetCourseInstructorPairs(String pathToFile) {
        DatasetProcessor reader = new DatasetProcessor();
        String course;
        float[] coursesWeeklyHours;
        String[] possibleCourses, coursesGroups, courseInstructorPairs;
        String[] tempHolder = new String[3];

        List listCourses = reader.readFile(pathToFile);
        possibleCourses = new String[listCourses.size()-1];
        coursesWeeklyHours = new float[listCourses.size()-1];
        courseInstructorPairs = new String[listCourses.size()-1];
        coursesGroups = new String[listCourses.size()-1];
        for(int i=1; i < listCourses.size(); i++) {
            course = listCourses.get(i).toString();
            tempHolder = course.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("&amp;", "&").split(";", 4);
            possibleCourses[i-1] = tempHolder[0];
            coursesWeeklyHours[i-1] = Float.parseFloat(tempHolder[1]);
            courseInstructorPairs[i-1] = tempHolder[2];
            coursesGroups[i-1] = tempHolder[3];
        }

        return courseInstructorPairs;
    }

    public static String[] psoGetCoursesGroups(String pathToFile) {
        DatasetProcessor reader = new DatasetProcessor();
        String course;
        float[] coursesWeeklyHours;
        String[] possibleCourses, coursesGroups, courseInstructorPairs;
        String[] tempHolder = new String[3];

        List listCourses = reader.readFile(pathToFile);
        possibleCourses = new String[listCourses.size()-1];
        coursesWeeklyHours = new float[listCourses.size()-1];
        courseInstructorPairs = new String[listCourses.size()-1];
        coursesGroups = new String[listCourses.size()-1];
        for(int i=1; i < listCourses.size(); i++) {
            course = listCourses.get(i).toString();
            tempHolder = course.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("&amp;", "&").split(";", 4);
            possibleCourses[i-1] = tempHolder[0];
            coursesWeeklyHours[i-1] = Float.parseFloat(tempHolder[1]);
            courseInstructorPairs[i-1] = tempHolder[2];
            coursesGroups[i-1] = tempHolder[3];
        }

        return coursesGroups;
    }

    public static String[] psoGetPossibleTimes(String pathToFile) {
        DatasetProcessor reader = new DatasetProcessor();
        String timeslot;
        String[] possibleTimes;
        float[] possibleTimesDurations;
        String[] tempHolder = new String[3];

        List listTimeslots = reader.readFile(pathToFile);
        possibleTimes = new String [listTimeslots.size()-1];
        possibleTimesDurations = new float [listTimeslots.size()-1];
        for(int i=1; i < listTimeslots.size(); i++) {
            timeslot = listTimeslots.get(i).toString();
            possibleTimes[i-1] = timeslot.replaceAll("\\[", "").replaceAll("\\]", "");
        }
        for(int i=0; i < possibleTimes.length; i++) {
            String[] slotCurrent = possibleTimes[i].replaceAll(" ", "").split("-", 2);
            String[] slotNext = new String[2];
            if(i < possibleTimes.length-1) {
                slotNext = possibleTimes[i+1].replaceAll(" ", "").split("-", 2);
            } else {
                slotNext = "Friday - 19:30".replaceAll(" ", "").split("-", 2);
            }
            if(slotCurrent[0].equals(slotNext[0])) {
                String[] slotCurrentTime = slotCurrent[1].split(":");
                int slotCurrentTimeMinutes = (Integer.parseInt(slotCurrentTime[0]) * 60) + (Integer.parseInt(slotCurrentTime[1]));
                String[] slotNextTime = slotNext[1].split(":", 2);
                int slotNextTimeMinutes = (Integer.parseInt(slotNextTime[0]) * 60) + (Integer.parseInt(slotNextTime[1]));
                int slotDifferenceMinutes = slotNextTimeMinutes - slotCurrentTimeMinutes;
                float slotDifferenceHours = (float)slotDifferenceMinutes/60;
                possibleTimesDurations[i] = slotDifferenceHours;
            } else {
                String[] slotCurrentTime = slotCurrent[1].split(":", 2);
                int slotCurrentTimeMinutes = (Integer.parseInt(slotCurrentTime[0]) * 60) + (Integer.parseInt(slotCurrentTime[1]));
                int slotNextTimeMinutes = (19 * 60) + 30;//last class should end at 19:30
                int slotDifferenceMinutes = slotNextTimeMinutes - slotCurrentTimeMinutes;
                float slotDifferenceHours = (float)slotDifferenceMinutes/60;
                possibleTimesDurations[i] = slotDifferenceHours;
            }
        }

        return possibleTimes;
    }

    public static float[] psoGetPossibleTimesDurations(String pathToFile) {
        DatasetProcessor reader = new DatasetProcessor();
        String timeslot;
        String[] possibleTimes;
        float[] possibleTimesDurations;
        String[] tempHolder = new String[3];

        List listTimeslots = reader.readFile(pathToFile);
        possibleTimes = new String [listTimeslots.size()-1];
        possibleTimesDurations = new float [listTimeslots.size()-1];
        for(int i=1; i < listTimeslots.size(); i++) {
            timeslot = listTimeslots.get(i).toString();
            possibleTimes[i-1] = timeslot.replaceAll("\\[", "").replaceAll("\\]", "");
        }
        for(int i=0; i < possibleTimes.length; i++) {
            String[] slotCurrent = possibleTimes[i].replaceAll(" ", "").split("-", 2);
            String[] slotNext = new String[2];
            if(i < possibleTimes.length-1) {
                slotNext = possibleTimes[i+1].replaceAll(" ", "").split("-", 2);
            } else {
                slotNext = "Friday - 19:30".replaceAll(" ", "").split("-", 2);
            }
            if(slotCurrent[0].equals(slotNext[0])) {
                String[] slotCurrentTime = slotCurrent[1].split(":");
                int slotCurrentTimeMinutes = (Integer.parseInt(slotCurrentTime[0]) * 60) + (Integer.parseInt(slotCurrentTime[1]));
                String[] slotNextTime = slotNext[1].split(":", 2);
                int slotNextTimeMinutes = (Integer.parseInt(slotNextTime[0]) * 60) + (Integer.parseInt(slotNextTime[1]));
                int slotDifferenceMinutes = slotNextTimeMinutes - slotCurrentTimeMinutes;
                float slotDifferenceHours = (float)slotDifferenceMinutes/60;
                possibleTimesDurations[i] = slotDifferenceHours;
            } else {
                String[] slotCurrentTime = slotCurrent[1].split(":", 2);
                int slotCurrentTimeMinutes = (Integer.parseInt(slotCurrentTime[0]) * 60) + (Integer.parseInt(slotCurrentTime[1]));
                int slotNextTimeMinutes = (19 * 60) + 30;//last class should end at 19:30
                int slotDifferenceMinutes = slotNextTimeMinutes - slotCurrentTimeMinutes;
                float slotDifferenceHours = (float)slotDifferenceMinutes/60;
                possibleTimesDurations[i] = slotDifferenceHours;
            }
        }

        return possibleTimesDurations;
    }

    public static String[] psoGetPossibleInstructors(String pathToFile) {
        DatasetProcessor reader = new DatasetProcessor();
        String instructor;
        String[] possibleInstructors, coursesPreferredTimes;
        String[] tempHolder = new String[3];

        List listInstructors = reader.readFile("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\instructors.csv");
        possibleInstructors = new String [listInstructors.size()-1];
        coursesPreferredTimes = new String [listInstructors.size()-1];
        for(int i=1; i < listInstructors.size(); i++) {
            instructor = listInstructors.get(i).toString();
            tempHolder = instructor.replaceAll("\\[", "").replaceAll("\\]", "").split(";", 2);
            possibleInstructors[i-1] = tempHolder[0];
            coursesPreferredTimes[i-1] = tempHolder[1];
        }

        return possibleInstructors;
    }

    public static String[] psoGetCoursesPreferredTimes(String pathToFile) {
        DatasetProcessor reader = new DatasetProcessor();
        String instructor;
        String[] possibleInstructors, coursesPreferredTimes;
        String[] tempHolder = new String[3];

        List listInstructors = reader.readFile("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\instructors.csv");
        possibleInstructors = new String [listInstructors.size()-1];
        coursesPreferredTimes = new String [listInstructors.size()-1];
        for(int i=1; i < listInstructors.size(); i++) {
            instructor = listInstructors.get(i).toString();
            tempHolder = instructor.replaceAll("\\[", "").replaceAll("\\]", "").split(";", 2);
            possibleInstructors[i-1] = tempHolder[0];
            coursesPreferredTimes[i-1] = tempHolder[1];
        }

        return coursesPreferredTimes;
    }
    
}
