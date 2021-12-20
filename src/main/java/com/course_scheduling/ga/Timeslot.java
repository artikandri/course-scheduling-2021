package com.course_scheduling.ga;

/**
 *
 * @author @artikandri
 */
public class Timeslot {

    public int id;
    public String time;
    public double duration;

    public Timeslot(int id, String time, double duration) {
        this.id = id;
        this.time = time;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public double getDuration() {
        return duration;
    }
}
