package com.course_scheduling.ga;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.IntStream;

public class Population {

    private ArrayList<Schedule> schedules;

    public Population(int size, Data data) {
        schedules = new ArrayList<>(size);
        IntStream.range(0, size).forEach(x -> schedules.add(new Schedule(data).initialize()));
    }

    public ArrayList<Schedule> getSchedules() {
        return this.schedules;
    }

    public Population sortByFitness() {
        schedules.sort((schedule1, schedule2) -> {
            int returnValue = 0;
            if (schedule1.getFitness() > schedule2.getFitness()) {
                returnValue = -1;
            } else if (schedule1.getFitness() < schedule2.getFitness()) {
                returnValue = 1;
            }
            return returnValue;
        });
        return this;
    }

    public Population sortByNumbOfConflicts() {
        schedules.sort((schedule1, schedule2) -> {
            int returnValue = 0;
            if (schedule1.getNumbOfConflicts() > schedule2.getNumbOfConflicts()) {
                returnValue = -1;
            } else if (schedule1.getNumbOfConflicts() < schedule2.getNumbOfConflicts()) {
                returnValue = 1;
            }
            return returnValue;
        });
        return this;
    }

    public Population sortByPenaltyAndNumbOfConflicts() {
        schedules.sort(Comparator.comparing(Schedule::getPenalty).thenComparing(Schedule::getNumbOfConflicts));
        return this;
    }

    public Population sortByPenalty() {
        schedules.sort((schedule1, schedule2) -> {
            int returnValue = 0;
            if (schedule1.getPenalty() > schedule2.getPenalty()) {
                returnValue = -1;
            } else if (schedule1.getPenalty() < schedule2.getPenalty()) {
                returnValue = 1;
            }
            return returnValue;
        });
        return this;
    }
}
