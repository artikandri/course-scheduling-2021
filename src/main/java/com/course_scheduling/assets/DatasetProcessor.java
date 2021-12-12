package com.course_scheduling.assets;

import com.opencsv.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Open dataset with given filename and columns()name
 * @params     String fileName, String[] columns
 * @return     [[]]
 *
 */
public class DatasetProcessor  {
    private static void readFile(String fileName, String columns) {
        fileName = fileName == null ? "/dataset/processed/courses.csv" : fileName;
        try {
            FileReader filereader = new FileReader(fileName);
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;
    
            while ((nextRecord = csvReader.readNext()) != null) {
                for (String cell : nextRecord) {
                    System.out.print(cell + "\t");
                }
                System.out.println();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args )
    {   
        readFile("../../../dataset/processed/courses.csv", "");
        System.out.print("test");
    }
    
}


