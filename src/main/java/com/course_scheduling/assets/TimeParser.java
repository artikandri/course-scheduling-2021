package com.course_scheduling.assets;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author @artikandri
 */
public class TimeParser {

    public int indexDayByName(String dayName) {
        String dayToFind = dayName.toLowerCase();
        List<String> daysOfTheWeek = Arrays.asList("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday");
        return daysOfTheWeek.indexOf(dayToFind);
    }

    public double timeDifferenceInHours(String startTime, String endTime) {
        ListParser listParser = new ListParser();
        List startTimes = listParser.arrayOfStringToListOfDoubles(startTime.split(":"));
        List endTimes = listParser.arrayOfStringToListOfDoubles(endTime.split(":"));

        Double durationHour = Double.parseDouble(startTimes.get(0).toString()) - Double.parseDouble(endTimes.get(0).toString());
        Double durationMin = Double.parseDouble(startTimes.get(1).toString()) - Double.parseDouble(endTimes.get(1).toString());

        durationHour = durationHour * 1;
        durationMin = durationMin / 60;

        return durationHour + durationMin;
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
