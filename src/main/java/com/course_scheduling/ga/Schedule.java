/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.course_scheduling.ga;

import java.util.*;

public class Schedule {

    private ArrayList<Class> classes;
    private boolean isFitnessChanged = true;
    private double fitness = -1;
    private int classNumb = 0;
    private int numbOfConflicts = 0;
    private Data data;

    public Data getData() {
        return data;
    }

    public Schedule(Data data) {
        this.data = data;
        classes = new ArrayList<Class>(data.getNumberOfClasses());
    }

    public Schedule initialize() {
        new ArrayList<Course>(data.getCourses()).forEach(course -> {
            Class newClass = new Class(classNumb++, course);
            newClass.setRoom(data.getRooms().get((int) (data.getRooms().size() * Math.random())));
            newClass.setInstructor(data.findInstructorById(course.getInstructorId()));

            double randomTimeslotId = data.getTimeslots().size() * Math.random();
            List preferredTimeslots = data.findInstructorById(course.getInstructorId()).preferences;

            if (!preferredTimeslots.isEmpty()) {
                double preferredTimeslotId = preferredTimeslots.size() * Math.random();
                newClass.setTimeslot(data.findTimeslotById((int) preferredTimeslots.get((int) preferredTimeslotId)));
            } else {
                newClass.setTimeslot(data.getTimeslots().get((int) randomTimeslotId));
            }

            classes.add(newClass);
        });
        return this;
    }

    public int getNumbOfConflicts() {
        return numbOfConflicts;
    }

    public ArrayList<Class> getClasses() {
        isFitnessChanged = true;
        return classes;
    }

    public double getFitness() {
        if (isFitnessChanged == true) {
            fitness = calculateFitness();
            isFitnessChanged = false;
        }
        return fitness;
    }

    private double calculateFitness() {
        numbOfConflicts = 0;
        classes.forEach(x -> {
            classes.stream().filter(y -> classes.indexOf(y) >= classes.indexOf(x)).forEach(y -> {
                if (x.getTimeslot() == y.getTimeslot() && x.getId() != y.getId()) {
                    if (x.getRoom() == y.getRoom()) {
                        numbOfConflicts++;
                    }
                    if (x.getInstructor() == y.getInstructor()) {
                        numbOfConflicts++;
                    }
                }
            });
        });
        return 1 / (double) (numbOfConflicts + 1);
    }

    public String toString() {
        String returnValue = new String();
        for (int x = 0; x < classes.size() - 1; x++) {
            returnValue += classes.get(x) + ",";
        }
        returnValue += classes.get(classes.size() - 1);
        return returnValue;
    }
}
