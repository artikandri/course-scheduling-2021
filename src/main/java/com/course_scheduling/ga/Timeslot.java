package com.course_scheduling.ga;

import com.course_scheduling.assets.TimeParser;

/**
 *
 * @author @artikandri
 */
public class Timeslot {

    private final int id;
    private final String time;
    private String day;
    private String hour;
    private final double duration;
    private int dayId;

    public Timeslot(int id, String time, double duration) {
        this.id = id;
        this.time = time;
        this.duration = duration;

        this.setDayAndHour();
        this.setDayId();
    }

    private void setDayId() {
        TimeParser timeParser = new TimeParser();
        this.dayId = timeParser.indexDayByName(day);
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

    public int getDayId() {
        return dayId;
    }
}
