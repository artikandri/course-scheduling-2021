package com.course_scheduling.ga;

import java.util.ArrayList;

public class Course {

    private int id;
    private String name = null;
    private double weeklyHours = 0;
    private int instructorId;

    public Course(int id, String name, double weeklyHours, int instructorId) {
        this.id = id;
        this.name = name;
        this.weeklyHours = weeklyHours;
        this.instructorId = instructorId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getWeeklyHours() {
        return weeklyHours;
    }

    public int getInstructorId() {
        return instructorId;
    }
}
