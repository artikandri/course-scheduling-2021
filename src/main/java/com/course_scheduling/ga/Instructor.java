package com.course_scheduling.ga;

import java.util.*;
import java.util.stream.*;

public final class Instructor {

    private final int id;
    private final String name;
    private final List<Integer> preferences;

    public Instructor(int id, String name, String preferences) {
        this.id = id;
        this.name = name;
        this.preferences = this.convertPreferencesToIntList(preferences);
    }

    public List convertPreferencesToIntList(String preferences) {
        List<Integer> list = Arrays.asList();
        if (!preferences.isEmpty()) {
            String[] prefs = preferences.replaceAll("\\]", "").replaceAll("\\s+", "").split(",", -1);
            list = Arrays.asList(prefs)
                    .stream()
                    .filter(item -> item != null && !"".equals(item) && !" ".equals(item))
                    .map(pref -> Integer.parseInt(pref.trim()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List getPreferences() {
        return preferences;
    }

}
