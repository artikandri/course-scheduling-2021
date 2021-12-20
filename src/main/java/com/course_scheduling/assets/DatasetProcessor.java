package com.course_scheduling.assets;

import com.opencsv.*;
import java.io.*;
import java.util.*;

/**
 * Open dataset with given filename and return records as arrays of columns.
 * Parse the columns by splitting it on ";" separator. The csv file uses ";"
 * separator because some of the column data contains ","
 *
 * @params String pathToCsv
 * @return List<String> records
 *
 */
public class DatasetProcessor {

    public static List readFile(String pathToCsv) {
        pathToCsv = pathToCsv == null ? "dataset/processed/courses.csv" : pathToCsv;
        List<List<String>> records = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader(pathToCsv));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }

}
