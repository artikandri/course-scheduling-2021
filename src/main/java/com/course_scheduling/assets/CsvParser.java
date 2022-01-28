package com.course_scheduling.assets;

/**
 *
 * @author @artikandri
 */
public class CsvParser {

    /**
     * parse columns in a row and return them as array of strings
     *
     * @param row a string of row
     * @return array of splitted columns in string
     *
     */
    public String[] parseRow(String row) {
        return row.replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .replaceAll("&amp;", "&")
                .split(";", -1);
    }
}
