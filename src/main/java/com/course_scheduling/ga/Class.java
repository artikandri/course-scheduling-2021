/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.course_scheduling.ga;

/**
 *
 * @author hp
 *
 */
public class Class {

    private int id;
    private Course course;
    private Instructor instructor;
    private Timeslot timeslot;
    private Room room;

    public Class(int id, Course course) {
        this.id = id;
        this.course = course;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public Room getRoom() {
        return room;
    }

    public String toString() {
        return "[" + "," + course.getName() + "," + room.getName() + "," + instructor.getName() + "," + timeslot.getTime() + "]";
    }
}
