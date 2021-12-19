package com.course_scheduling.ga;

import com.course_scheduling.assets.DatasetProcessor;
import java.util.*;

public class Course {

    public int id;
    public String name = null;
    public double weeklyHours = 0;
    public int instructorId;
    public String instructorName;

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

    public String getInstructorName() {
        return instructorName;
    }
}
