package com.course_scheduling.ga;

/**
 *
 * @author @artikandri
 */
import com.course_scheduling.assets.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class Data {

    private ArrayList<Room> rooms;
    private ArrayList<Instructor> instructors;
    private ArrayList<Course> courses;
    private ArrayList<Timeslot> timeslots;
    private ArrayList<Group> groups;

    private int numberOfClasses = 0;
    private CsvParser csvParser = new CsvParser();
    private TimeParser timeParser = new TimeParser();

    public Data() {
        initialize();
    }

    private Data initialize() {
        setRooms();
        setInstructors();
        setCourses();
        setTimeslots();
        setGroups();
        setNumberOfClasses();

        return this;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public ArrayList<Instructor> getInstructors() {
        return instructors;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public ArrayList<Timeslot> getTimeslots() {
        return timeslots;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public int getNumberOfClasses() {
        return this.numberOfClasses;
    }

    public void setRooms() {
        List roomCsvs = DatasetProcessor.readFile("dataset/processed/rooms.csv");
        roomCsvs.remove(0);
        rooms = new ArrayList<Room>(Arrays.asList());
        for (int i = 0; i < roomCsvs.size(); i++) {
            String[] row = csvParser.parseRow(roomCsvs.get(i).toString());
            Room room = new Room(i, row[0]);
            rooms.add(room);
        }
    }

    public void setInstructors() {
        List instructorCsvs = DatasetProcessor.readFile("dataset/processed/instructors.csv");
        instructorCsvs.remove(0);
        instructors = new ArrayList<Instructor>(Arrays.asList());
        for (int i = 0; i < instructorCsvs.size(); i++) {
            String[] row = csvParser.parseRow(instructorCsvs.get(i).toString());
            Instructor instructor = new Instructor(i, row[0], row[1]);
            instructors.add(instructor);
        }
    }

    public void setCourses() {
        List courseCsvs = DatasetProcessor.readFile("dataset/processed/courses.csv");
        courseCsvs.remove(0);
        courses = new ArrayList<Course>(Arrays.asList());
        for (int i = 0; i < courseCsvs.size(); i++) {
            String[] row = csvParser.parseRow(courseCsvs.get(i).toString());
            Course course = new Course(i, row[0], Double.parseDouble(row[1]),
                    Integer.parseInt(row[2].replaceAll("\\]", "")));
            courses.add(course);
        }
    }

    public void setGroups() {
        List courseCsvs = DatasetProcessor.readFile("dataset/processed/courses.csv");
        List groupCsvs = new ArrayList(Arrays.asList());
        courseCsvs.remove(0);
        groups = new ArrayList<Group>(Arrays.asList());
        for (int i = 0; i < courseCsvs.size(); i++) {
            String[] row = csvParser.parseRow(courseCsvs.get(i).toString());
            groupCsvs.add(row[3]);
        }

        // make unique
        // map as group object via loop
        // impor di schedule.java
        // ubah fitness function
        // sesuaikan jadwal
    }

    public void setTimeslots() {
        List timeslotCsvs = DatasetProcessor.readFile("dataset/processed/timeslots.csv");
        timeslotCsvs.remove(0);
        timeslots = new ArrayList<Timeslot>(Arrays.asList());
        for (int i = 0; i < timeslotCsvs.size(); i++) {
            String[] row = csvParser.parseRow(timeslotCsvs.get(i).toString());
            String time = row[0].split(" - ")[1];
            String courseTime = timeParser.padZeroToHourAndMinute(time);

            String startTime = "07:30:00";
            String endTime = "19:30:00";
            double duration = 1;

            // calculate duration for each timeslot
            if (i == 0) {
                duration = timeParser.timeDifferenceInHours(startTime, courseTime);
            } else if (i == timeslotCsvs.size() - 1) {
                duration = timeParser.timeDifferenceInHours(courseTime, endTime);
            } else {
                String[] nextRow = csvParser.parseRow(timeslotCsvs.get(i + 1).toString());
                String nextTime = nextRow[0].split(" - ")[1];
                String nextCourseTime = timeParser.padZeroToHourAndMinute(nextTime);

                duration = timeParser.timeDifferenceInHours(courseTime, nextCourseTime);
            }

            Timeslot timeslot = new Timeslot(i, row[0], duration);
            timeslots.add(timeslot);
        }
    }

    public Instructor findInstructorById(int id) {
        return instructors.stream().filter(instructor -> instructor.id == id).findFirst()
                .orElse(null);
    }

    public Course findCourseById(int id) {
        return courses.stream().filter(course -> course.id == id).findFirst().orElse(null);
    }

    public Timeslot findTimeslotById(int id) {
        return timeslots.stream().filter(timeslot -> timeslot.id == id).findFirst().orElse(null);
    }

    public void setNumberOfClasses() {
        numberOfClasses = courses.size();
    }

    public void setData() {
        setRooms();
        setInstructors();
        setCourses();
        setTimeslots();
        setGroups();
        setNumberOfClasses();
    }

    public static void main(String[] args) {
        Data data = new Data();
        data.setData();
    }
}
