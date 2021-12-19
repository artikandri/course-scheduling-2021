/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.course_scheduling.assets;

/**
 *
 * @author hp
 */
public class CsvParser {

    public String[] parseRow(String row) {
        return row.replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .replaceAll("&amp;", "&")
                .split(";", -1);
    }
}
