package com.course_scheduling.assets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author @artikandri
 */
public class FileManager {

    public void createTextFile(String output, String fileName, String filePath) {

        String content = output.isBlank() ? "Text" : output;
        String fName = fileName.isBlank() ? "Output" : fileName;
        String path = filePath.isBlank() ? "results/" : filePath;

        try {
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory + "/" + fName);
            try {
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(content);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // function is not used
    // only here to help debugging
    // to do: remove before submission
    public static void main(String[] args) {
        FileManager fileManager = new FileManager();
        fileManager.createTextFile("test", "test.txt", "results/ga/");
    }

}
