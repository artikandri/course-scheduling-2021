package com.course_scheduling.ga;

import java.util.*;
import java.util.stream.Collectors;

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

            // consider timeslot
            List<Integer> preferredTimeslots = data.findInstructorById(course.getInstructorId()).getPreferences();
            List<Timeslot> preferredTimeslotObjects = data.getTimeslots()
                    .stream()
                    .filter(timeslot -> preferredTimeslots.contains(timeslot.getId()))
                    .collect(Collectors.toList());

            // consider duration
            List possibleDurations = course.getPossibleDurations();
            double randomPossibleDurationId = possibleDurations.size() * Math.random();
            List<Double> randomDurations = (List) possibleDurations.get((int) randomPossibleDurationId);

            // match duration with timeslot
            // add classes by considering the duration
            // > 1 hour means multiple classes
            List<Integer> previouslyChosenTimeslotIds = new ArrayList(Arrays.asList());
            Map possibleTimeslotMap = course.getPossibleTimeslotMap();

            for (Double duration : randomDurations) {
                Class newClass = new Class(classNumb++, course);
                newClass.setRoom(data.getRooms().get((int) (data.getRooms().size() * Math.random())));
                newClass.setInstructor(data.findInstructorById(course.getInstructorId()));

                List<Integer> possibleTimeslots = (List) possibleTimeslotMap.get(duration.toString());
                List<Integer> suitablePreferredTimeslots = Arrays.asList();
                List<Timeslot> suitablePreferredTimeslotObjects = Arrays.asList();
                List<Timeslot> suitableTimeslotObjects = Arrays.asList();

                if (possibleTimeslots != null) {
                    suitablePreferredTimeslots = preferredTimeslots
                            .stream()
                            .collect(Collectors.filtering(timeslotId
                                    -> possibleTimeslots.contains(timeslotId),
                                    Collectors.toList()));

                    suitablePreferredTimeslotObjects = data.getTimeslots()
                            .stream()
                            .collect(Collectors.filtering(timeslot
                                    -> suitablePreferredTimeslots.contains(timeslot.getId()),
                                    Collectors.toList()));

                    suitableTimeslotObjects = data.getTimeslots()
                            .stream()
                            .collect(Collectors.filtering(timeslot
                                    -> possibleTimeslots.contains(timeslot.getId()),
                                    Collectors.toList()));
                }

                if (suitablePreferredTimeslots.isEmpty()) {
                    // assign random timeslot with possible duration
                    boolean isTimeslotSet = false;
                    do {
                        double randomTimeslotId = suitableTimeslotObjects.size() * Math.random();
                        Timeslot randomTimeslot = suitableTimeslotObjects.get((int) randomTimeslotId);
                        if (previouslyChosenTimeslotIds.isEmpty() || !previouslyChosenTimeslotIds.contains((int) randomTimeslot.getId())) {
                            previouslyChosenTimeslotIds.add(randomTimeslot.getId());
                            newClass.setTimeslot(randomTimeslot);
                            classes.add(newClass);
                            isTimeslotSet = true;
                        }

                    } while (!isTimeslotSet);
                } else {
                    // assign random timeslot from preferred timeslots if possible
                    // if not, assign from timeslot pool
                    boolean isTimeslotSet = false;
                    do {
                        Timeslot timeslotObject = suitableTimeslotObjects.get((int) (suitableTimeslotObjects.size() * Math.random()));
                        if (suitablePreferredTimeslots.contains(timeslotObject.getId())) {
                            if (previouslyChosenTimeslotIds.isEmpty() || !previouslyChosenTimeslotIds.contains((int) timeslotObject.getId())) {
                                previouslyChosenTimeslotIds.add(timeslotObject.getId());
                                newClass.setTimeslot(timeslotObject);

                                classes.add(newClass);
                                isTimeslotSet = true;
                            }
                        }

                        if (!isTimeslotSet) {
                            // do something if isTimeslotSet is still false
                            double randomTimeslotId = suitableTimeslotObjects.size() * Math.random();
                            Timeslot randomTimeslot = suitableTimeslotObjects.get((int) randomTimeslotId);
                            if (previouslyChosenTimeslotIds.isEmpty() || !previouslyChosenTimeslotIds.contains((int) randomTimeslot.getId())) {
                                previouslyChosenTimeslotIds.add(randomTimeslot.getId());
                                newClass.setTimeslot(randomTimeslot);
                                classes.add(newClass);
                                isTimeslotSet = true;
                            }
                        }
                    } while (!isTimeslotSet);
                }
            }
            previouslyChosenTimeslotIds.clear();
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

    public static void main(String[] args) {
        Data data = new Data();
        Schedule schedule = new Schedule(data);
        schedule.initialize();
    }

}
