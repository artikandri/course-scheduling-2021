package com.course_scheduling.ga;

/**
 *
 * @author @artikandri
 *
 */
public class Class {

    private int id;
    private Course course;
    private Instructor instructor;
    private Timeslot timeslot;
    private Room room;
    private Group group;
    private boolean isFlagged = false;

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

    public void setGroup(Group group) {
        this.group = group;
    }

    public boolean getIsFlagged() {
        return isFlagged;
    }

    public int getId() {
        return id;
    }

    public Course getCourse() {
        return course;
    }

    public Group getGroup() {
        return group;
    }

    public int getGroupId() {
        return group.getId();
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public int getTimeslotId() {
        return timeslot.getId();
    }

    public int getCourseId() {
        return course.getId();
    }

    public int getTimeslotDayId() {
        return timeslot.getDayId();
    }

    public Room getRoom() {
        return room;
    }

    public void setIsFlagged(boolean isFlagged) {
        this.isFlagged = isFlagged;
    }
}
