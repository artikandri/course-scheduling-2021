package com.course_scheduling.assets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 *
 * @author @artikandri
 */
public class OutputWriter {

    public void createTextFile(String output, String fileName, String filePath) {
        String content = output.isBlank() ? "Text" : output;
        String localDir = System.getProperty("user.dir");
        String name = fileName.isBlank() ? "Output" : fileName;
        String path = filePath.isBlank() ? localDir + fileName + ".txt" : filePath;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
