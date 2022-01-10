package com.course_scheduling.app;

import com.course_scheduling.ga.Scheduler;
import com.course_scheduling.pso.pso;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Constructor.
 *
 * Called in pom.xml
 *
 */
public class App {

    public static void main(String[] args) {
        // run GA algorithm
        Scheduler driver = new Scheduler();
        driver.runAlgorithm();

        //run PSO algorithm
        float[] coursesWeeklyHours;
        String[] possibleRooms, possibleCourses, possibleTimes, possibleInstructors, coursesGroups, coursesPreferredTimes, courseInstructorPairs;
        float[] possibleTimesDurations;
        //("C:\\Users\\Teddy Ferdinan\\Desktop\\courses_medium.csv");
        //("C:\\Users\\Teddy Ferdinan\\Desktop\\courses_large.csv");
        //("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\courses.csv");
        possibleCourses = pso.psoGetPossibleCourses("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\courses.csv");
        coursesWeeklyHours = pso.psoGetCoursesWeeklyHours("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\courses.csv");
        courseInstructorPairs = pso.psoGetCourseInstructorPairs("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\courses.csv");
        coursesGroups = pso.psoGetCoursesGroups("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\courses.csv");
        possibleRooms = pso.psoGetPossibleRooms("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\rooms.csv");
        possibleTimes = pso.psoGetPossibleTimes("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\timeslots.csv");
        possibleTimesDurations = pso.psoGetPossibleTimesDurations("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\timeslots.csv");
        possibleInstructors = pso.psoGetPossibleInstructors("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\instructors.csv");
        coursesPreferredTimes = pso.psoGetCoursesPreferredTimes("D:\\GithubRepos\\course-scheduling-2021\\dataset\\processed\\instructors.csv");
        String logOutput = "";        
        //initialize PSO
        //Configuration x = psoInitialize("limitFV", 750, 30, possibleRooms, possibleCourses, possibleTimes, possibleTimesDurations, coursesGroups, coursesWeeklyHours, coursesPreferredTimes, courseInstructorPairs);
        Configuration x = pso.psoInitialize("limitIter", 1000, 30, possibleRooms, possibleCourses, possibleTimes, possibleTimesDurations, coursesGroups, coursesWeeklyHours, coursesPreferredTimes, courseInstructorPairs);
        Configuration xNew = psoIterate(x, "");
        logOutput += xNew.show();
        logOutput += psoMapSchedules(xNew);
        //write log output to file
        try {
            File myObj = new File("C:\\Users\\Teddy Ferdinan\\Desktop\\output.txt");
            if(myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try {
            FileWriter myWriter = new FileWriter("C:\\Users\\Teddy Ferdinan\\Desktop\\output.txt");
            myWriter.write(logOutput);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
