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

            // consider timeslot
            List preferredTimeslots = data.findInstructorById(course.getInstructorId()).getPreferences();

            // consider duration
            List possibleDurations = course.getPossibleDurations();
            double randomPossibleDurationId = possibleDurations.size() * Math.random();
            List randomDurations = (List) possibleDurations.get((int) randomPossibleDurationId);

            // match duration with timeslot
            // add classes by considering the duration
            // > 1 hour means multiple classes
            for (var duration : randomDurations) {
                if (preferredTimeslots.isEmpty()) {
                    // assign random timeslot with possible duration
                    boolean isTimeslotSet = false;
                    do {
                        double randomTimeslotId = data.getTimeslots().size() * Math.random();
                        Timeslot randomTimeslot = data.getTimeslots().get((int) randomTimeslotId);
                        if (randomTimeslot.getDuration() == Double.parseDouble(duration.toString())) {
                            newClass.setTimeslot(randomTimeslot);
                            classes.add(newClass);
                            isTimeslotSet = true;
                        }

                    } while (!isTimeslotSet);
                } else {
                    // assign preferred timeslot if duration matches
                    // else assign random timeslot
                    Integer chosenPreferredTimeslotId;
                    List<Integer> previouslyChosenPreferredTimeslotId = new ArrayList(Arrays.asList());
                    boolean isTimeslotSet = false;
                    int i = 0;
                    do {
                        // choose preferred timeslot id from preference list
                        // do not assign the same preferred timeslot as previous one
                        // filter possible timeslot from preferred timeslot
                        // only assign them
                        // if not possible
                        boolean isTimeslotChosen = false;
                        do {
                            double randomPreferredTimeslotId = preferredTimeslots.size() * Math.random();
                            chosenPreferredTimeslotId = Integer.parseInt(preferredTimeslots.get((int) randomPreferredTimeslotId).toString());
                            if (!previouslyChosenPreferredTimeslotId.contains(chosenPreferredTimeslotId)) {
                                previouslyChosenPreferredTimeslotId.add(chosenPreferredTimeslotId);
                                isTimeslotChosen = true;
                            }
                        } while (!isTimeslotChosen);

                        // clear list
                        previouslyChosenPreferredTimeslotId.clear();

                        // assign timeslot
                        Timeslot randomPreferredTimeslot = data.findTimeslotById((int) chosenPreferredTimeslotId);
                        double randomTimeslotId = data.getTimeslots().size() * Math.random();
                        Timeslot randomTimeslot = data.getTimeslots().get((int) randomTimeslotId);

                        if (randomPreferredTimeslot.getDuration() == Double.parseDouble(duration.toString())) {
                            newClass.setTimeslot(randomPreferredTimeslot);
                            classes.add(newClass);

                            isTimeslotSet = true;
                        } else if (randomTimeslot.getDuration() == Double.parseDouble(duration.toString())) {
                            newClass.setTimeslot(randomTimeslot);
                            classes.add(newClass);

                            isTimeslotSet = true;
                        }
                    } while (!isTimeslotSet);
                }

            }
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
