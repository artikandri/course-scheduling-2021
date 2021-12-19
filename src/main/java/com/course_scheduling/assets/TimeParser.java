/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.course_scheduling.assets;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author hp
 */
public class TimeParser {

    public double timeDifferenceInHours(String startTime, String endTime) {
        return Duration.between(LocalTime.parse(endTime), LocalTime.parse(startTime))
                .toHours();
    }

    public String padZeroToHourAndMinute(String time) {
        String[] hourAndMinute = time.split(":");
        List hourAndMinuteList = Arrays.asList(hourAndMinute)
                .stream()
                .filter(item -> item != null && !"".equals(item) && !" ".equals(item))
                .map(pref -> Integer.parseInt(pref.trim()))
                .collect(Collectors.toList());

        String parsedTime = String.format("%02d:%02d", hourAndMinuteList.get(0), hourAndMinuteList.get(1)) + ":00";
        return parsedTime;
    }

}
