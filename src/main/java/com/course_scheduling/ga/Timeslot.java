package com.course_scheduling.ga;

/**
 *
 * @author @artikandri
 */
public class Timeslot {

    private int id;
    private String time;
    private String day;
    private String hour;
    private double duration;

    public Timeslot(int id, String time, double duration) {
        this.id = id;
        this.time = time;
        this.duration = duration;

        this.setDayAndHour();
    }

    private void setDayAndHour() {
        String[] dayAndHour = time.split(" - ");
        day = dayAndHour[0];
        hour = dayAndHour[1];
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
