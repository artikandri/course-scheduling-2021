package com.course_scheduling.assets;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author @artikandri
 */
public class TimeParser {

    /**
     * Calculates the index (order) of a day by their name in a week. Starts
     * from Monday
     *
     * @param dayName the day to be indexed
     * @return index (order) of a day
     */
    public int indexDayByName(String dayName) {
        String dayToFind = dayName.toLowerCase();
        List<String> daysOfTheWeek = Arrays.asList("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday");
        return daysOfTheWeek.indexOf(dayToFind);
    }

    /**
     * Calculate the time difference of two given times in hours
     *
     * @param startTime start time (HH:mm)
     * @param endTime end time (HH:mm)
     * @return double the time difference
     */
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

    /**
     * Pad zero to hour and minute in time (ex: 7:30 -> 07:30)
     *
     * @return padded time as string
     */
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
