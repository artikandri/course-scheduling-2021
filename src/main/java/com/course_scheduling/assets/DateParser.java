package com.course_scheduling.assets;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author @artikandri
 */
public class DateParser {

    /**
     * Get today date in a given date format. When format not given, will return
     * the date in default format (dd/MM/yyyy HH:mm:ss)
     *
     * @param dateTime date format
     * @return today's date as string
     */
    public String getTodayDate(String... dateFormat) {
        String dFormat = dateFormat.length == 0 ? "dd/MM/yyyy HH:mm:ss" : dateFormat[0];
        SimpleDateFormat formatter = new SimpleDateFormat(dFormat);
        Date date = new Date();

        return formatter.format(date);
    }

}
