package com.course_scheduling.assets;

/**
 *
 * @author @artikandri
 */
public class CsvParser {

    public String[] parseRow(String row) {
        return row.replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .replaceAll("&amp;", "&")
                .split(";", -1);
    }
}
