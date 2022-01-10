package com.course_scheduling.app;

import com.course_scheduling.ga.Scheduler;
import com.course_scheduling.pso.pso;
import com.course_scheduling.pso.pso.Configuration;
import com.course_scheduling.assets.FileManager;

/**
 * Constructor.
 *
 * Called in pom.xml
 *
 */
public class App {
    
    private final FileManager fileManager = new FileManager();

    public static void main(String[] args) {
        
        App app = new App();
        // run GA algorithm
//        Scheduler driver = new Scheduler();
//        driver.runAlgorithm();

        //run PSO algorithm
        app.runPsoAlgorithm();
    }
    
    private void runPsoAlgorithm() {
        float[] coursesWeeklyHours;
        String[] possibleRooms, possibleCourses, possibleTimes, possibleInstructors, coursesGroups, coursesPreferredTimes, courseInstructorPairs;
        float[] possibleTimesDurations;
        possibleCourses = pso.psoGetPossibleCourses("src/main/resources/dataset/processed/courses.csv");
        coursesWeeklyHours = pso.psoGetCoursesWeeklyHours("src/main/resources/dataset/processed/courses.csv");
        courseInstructorPairs = pso.psoGetCourseInstructorPairs("src/main/resources/dataset/processed/courses.csv");
        coursesGroups = pso.psoGetCoursesGroups("src/main/resources/dataset/processed/courses.csv");
        possibleRooms = pso.psoGetPossibleRooms("src/main/resources/dataset/processed/rooms.csv");
        possibleTimes = pso.psoGetPossibleTimes("src/main/resources/dataset/processed/timeslots.csv");
        possibleTimesDurations = pso.psoGetPossibleTimesDurations("src/main/resources/dataset/processed/timeslots.csv");
        possibleInstructors = pso.psoGetPossibleInstructors("src/main/resources/dataset/processed/instructors.csv");
        coursesPreferredTimes = pso.psoGetCoursesPreferredTimes("src/main/resources/dataset/processed/instructors.csv");
        String logOutput = "";        
        //initialize PSO
        //Configuration x = psoInitialize("limitFV", 750, 30, possibleRooms, possibleCourses, possibleTimes, possibleTimesDurations, coursesGroups, coursesWeeklyHours, coursesPreferredTimes, courseInstructorPairs);
        
        Configuration x = pso.psoInitialize("limitIter", 1000, 30, possibleRooms, possibleCourses, possibleTimes, possibleTimesDurations, coursesGroups, coursesWeeklyHours, coursesPreferredTimes, courseInstructorPairs);
        Configuration xNew = pso.psoIterate(x, "");
        logOutput += xNew.show();
        logOutput += pso.psoMapSchedules(xNew);
        //write log output to file
        
        fileManager.createTextFile(logOutput, "Schedules-PSO", "results/pso/");
       
    }

}
