package com.course_scheduling.ga;

/**
 *
 * @author @artikandri
 */
public class Group {

    private int id;
    private String name;

    public Group(int id) {
        this.id = id;
        this.name = "Group " + Integer.toString(id);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
