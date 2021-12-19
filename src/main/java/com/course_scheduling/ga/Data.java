/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.course_scheduling.ga;

/**
 *
 * @author hp
 */
import com.course_scheduling.assets.DatasetProcessor;
import java.util.*;

public class Data {

    private ArrayList<Room> rooms;
    private ArrayList<Instructor> instructors;
    private ArrayList<Course> courses;
    private ArrayList<Timeslot> timeslots;
    private int numberOfClasses = 0;

    public Data() {
        initialize();
    }

    private Data initialize() {
        setRooms();
        setInstructors();
        setCourses();
        setTimeslots();
        setNumberOfClasses();

        return this;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public ArrayList<Instructor> getInstructors() {
        return instructors;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public ArrayList<Timeslot> getTimeslots() {
        return timeslots;
    }

    public int getNumberOfClasses() {
        return this.numberOfClasses;
    }

    public void setRooms() {
        List roomCsvs = DatasetProcessor.readFile("dataset/processed/rooms.csv");
        roomCsvs.remove(0);
        rooms = new ArrayList<Room>(Arrays.asList());
        for (int i = 0; i < roomCsvs.size(); i++) {
            Room room = new Room(i, roomCsvs.get(i).toString());
            rooms.add(room);
        }
    }

    public void setInstructors() {
        List instructorCsvs = DatasetProcessor.readFile("dataset/processed/instructors.csv");
        instructorCsvs.remove(0);
        instructors = new ArrayList<Instructor>(Arrays.asList());
        for (int i = 0; i < instructorCsvs.size(); i++) {
            String[] row = instructorCsvs.get(i).toString().split(";", -1);
            Instructor instructor = new Instructor(i, row[0], row[1]);
            instructors.add(instructor);
        }
    }

    public void setCourses() {
        List courseCsvs = DatasetProcessor.readFile("dataset/processed/courses.csv");
        courseCsvs.remove(0);
        courses = new ArrayList<Course>(Arrays.asList());
        for (int i = 0; i < courseCsvs.size(); i++) {
            String[] row = courseCsvs.get(i).toString().replaceAll("&amp;", "&").split(";", -1);
            Course course = new Course(i, row[0], Double.parseDouble(row[1]), Integer.parseInt(row[2].replaceAll("\\]", "")));
            courses.add(course);
        }
    }

    public void setTimeslots() {
        List timeslotCsvs = DatasetProcessor.readFile("dataset/processed/timeslots.csv");
        timeslotCsvs.remove(0);
        timeslots = new ArrayList<Timeslot>(Arrays.asList());
        for (int i = 0; i < timeslotCsvs.size(); i++) {
            String[] row = timeslotCsvs.get(i).toString().replaceAll("&amp;", "&").split(";", -1);
            Timeslot timeslot = new Timeslot(i, row[0]);
            timeslots.add(timeslot);
        }
    }

    public void setNumberOfClasses() {
        numberOfClasses = courses.size();
    }

    public void setData() {
        setRooms();
        setInstructors();
        setCourses();
        setTimeslots();
        setNumberOfClasses();
    }
}
