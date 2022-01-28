package com.course_scheduling.ga;

import java.util.ArrayList;

/**
 *
 * @author @artikandri
 */
public class Population {

    private ArrayList<Schedule> schedules;

    public Population(int size, Data data) {
        schedules = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            schedules.add(new Schedule(data).initialize());
        }
    }

    public ArrayList<Schedule> getSchedules() {
        return this.schedules;
    }

    public Population sortByFitness() {
        schedules.sort((schedule1, schedule2) -> {
            Penalty penalty1 = new Penalty(schedule1);
            Penalty penalty2 = new Penalty(schedule2);

            int sortScore = penalty1.getFitness() > penalty2.getFitness() ? -1 : 1;
            return sortScore;
        });
        return this;
    }

    public Population sortByNumbOfConflicts() {
        schedules.sort((schedule1, schedule2) -> {
            Penalty penalty1 = new Penalty(schedule1);
            Penalty penalty2 = new Penalty(schedule2);

            int sortScore = penalty1.getNumbOfConflicts() > penalty2.getNumbOfConflicts() ? -1 : 1;
            return sortScore;
        });
        return this;
    }

    public Population sortByPenaltyAndNumbOfConflicts() {
        sortByPenalty();
        sortByNumbOfConflicts();

        return this;
    }

    public Population sortByPenalty() {
        schedules.sort((schedule1, schedule2) -> {
            Penalty penalty1 = new Penalty(schedule1);
            Penalty penalty2 = new Penalty(schedule2);

            int sortScore = penalty1.getPenalty() > penalty2.getPenalty() ? -1 : 1;
            return sortScore;
        });
        return this;
    }
}
