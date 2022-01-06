package com.course_scheduling.ga;

import com.course_scheduling.assets.DatasetProcessor;
import java.util.*;

public class Course {

    private int id;
    private String name = null;
    private double weeklyHours = 0;
    private int instructorId;
    private int groupId;

    private List possibleDurations;
    private List possibleTimeslots;
    private int maxPossibleCombination;

    public Course(int id, String name, double weeklyHours, int instructorId, int groupId) {
        this.id = id;
        this.name = name;
        this.weeklyHours = weeklyHours;
        this.instructorId = instructorId;
        this.groupId = groupId;
    }

    public void setPossibleDurations(List possibleDurations) {
        this.possibleDurations = possibleDurations;
    }

    public void setPossibleTimeslots(List possibleTimeslots) {
        this.possibleTimeslots = possibleTimeslots;
    }

    public void setMaxPossibleCombination(int maxPossibleCombination) {
        this.maxPossibleCombination = maxPossibleCombination;
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

    public int getGroupId() {
        return groupId;
    }

    public List getPossibleDurations() {
        return possibleDurations;
    }

    public List getPossibleTimeslots() {
        return possibleTimeslots;
    }

    public int getMaxPossibleCombination() {
        return maxPossibleCombination;
    }
}
