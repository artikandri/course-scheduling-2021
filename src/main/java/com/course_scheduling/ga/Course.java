package com.course_scheduling.app;

import org.apache.commons.lang3.ArrayUtils;

public class Course {

    private int courseId; 
    private String courseName; 
    private int instructorId; 
    private int defaultDay; 
    private int fixedTimeSlot; 
    private int fixedRoom; 
    private int unavailableTime[];

    public Course() {

    }

    void setCourseData(int courseId, String courseName, int instructorId, int defaultDay, int fixedTimeSlot, int fixedRoom, int[] unavailableTime) {
        this.courseId = courseId; 
        this.courseName = courseName; 
        this.instructorId = instructorId; 
        this.defaultDay = defaultDay; 
        this.fixedTimeSlot = fixedTimeSlot; 
        this.fixedRoom = fixedRoom; 
        
        // checks unavailable time 
        if(ArrayUtils.isNotEmpty(unavailableTime)) {
            // do something
        }
    }

}