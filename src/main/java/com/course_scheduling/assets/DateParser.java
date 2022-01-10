package com.course_scheduling.assets;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author @artikandri
 */
public class DateParser {

    public String getTodayDate(String... dateFormat) {
        String dFormat = dateFormat.length == 0 ? "dd/MM/yyyy HH:mm:ss" : dateFormat[0];
        SimpleDateFormat formatter = new SimpleDateFormat(dFormat);
        Date date = new Date();

        return formatter.format(date);
    }

    public static void main(String[] args) {
        DateParser dateParser = new DateParser();
        dateParser.getTodayDate();
    }

}
