package com.course_scheduling.ga;

import org.apache.commons.lang3.ArrayUtils;

public class Course {
    private int id; 
    private String courseName; 
    private int instructorId; 

    public Course() {

    }

    public void setCourseData(int courseId, String courseName, int instructorId) {
        this.id = courseId; 
        this.courseName = courseName; 
        this.instructorId = instructorId; 

        // do something
    }

}