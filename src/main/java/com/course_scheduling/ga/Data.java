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
    private ListParser listParser = new ListParser();

    public Data() {
        initialize();
    }

    private Data initialize() {
        setRooms();
        setInstructors();
        setTimeslots();
        setGroups();
        setCourses();
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

            // set possible duration combinations for each course
            // the combinations will be checked on constraint checking
            // result: list of list of doubles
            List<List> possibleDurations = new ArrayList(Arrays.asList());
            ArrayList<Double> durations = new ArrayList<>(Arrays.asList(1.0, 1.5));
            int maxLength = 0;

            NumberCombinator numberCombinator = new NumberCombinator(durations, Double.parseDouble(row[1].replaceAll("\\]", "")), true);
            numberCombinator.calculateCombinations();
            for (String solution : numberCombinator.getCombinations()) {
                String[] combinations = solution.replaceAll("\\[", "").replaceAll("\\]", "").split(" , ");
                for (String combination : combinations) {
                    String result = combination.substring(combination.lastIndexOf(":") + 1);
                    String[] results = result.replaceAll("\\[", "").replaceAll("\\]", "").split(" ");
                    maxLength = results.length > maxLength ? results.length : maxLength;
                    possibleDurations.add(listParser.arrayOfStringToListOfDoubles(results));
                }
            }

            // set possible timeslots combinations for each course
            // the combinations will be checked on constraint checking
            // result: list of list of object
            List<List> possibleTimeslots = new ArrayList(Arrays.asList());
            for (List possibleDuration : possibleDurations) {
                List possibleTimeslot = new ArrayList(Arrays.asList());
                for (var duration : possibleDuration) {
                    boolean isTimeslotSet = false;
                    do {
                        double randomTimeslotId = timeslots.size() * Math.random();
                        Timeslot timeslot = findTimeslotById((int) randomTimeslotId);

                        if (timeslot.getDuration() == Double.parseDouble(duration.toString())) {
                            possibleTimeslot.add(timeslot.getId());
                            isTimeslotSet = true;
                        }

                    } while (!isTimeslotSet);
                }
                possibleTimeslots.add(possibleTimeslot);
            }

            Course course = new Course(i, row[0], Double.parseDouble(row[1]),
                    Integer.parseInt(row[2].replaceAll("\\]", "")),
                    Integer.parseInt(row[3].replaceAll("\\]", ""))
            );

            course.setPossibleDurations(possibleDurations);
            course.setPossibleTimeslots(possibleTimeslots);
            course.setMaxPossibleCombination(maxLength);

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

        List uniqueGroups = listParser.uniqueList(groupCsvs);
        for (int i = 0; i < uniqueGroups.size(); i++) {
            Group group = new Group(Integer.parseInt(uniqueGroups.get(i).toString()));
            groups.add(group);
        }
    }

    public void setTimeslots() {
        List timeslotCsvs = DatasetProcessor.readFile("dataset/processed/timeslots.csv");
        timeslotCsvs.remove(0);
        timeslots = new ArrayList<Timeslot>(Arrays.asList());
        for (int i = 0; i < timeslotCsvs.size(); i++) {
            String[] row = csvParser.parseRow(timeslotCsvs.get(i).toString());
            String time = row[0].split(" - ")[1];
            String courseTime = timeParser.padZeroToHourAndMinute(time).trim();

            String startTime = "07:30:00";
            String endTime = "19:30:00";
            double duration = 1.0;

            // calculate duration for each timeslot
            if (i == timeslotCsvs.size() - 1) {
                duration = timeParser.timeDifferenceInHours(endTime, courseTime);
            } else {
                String[] nextRow = csvParser.parseRow(timeslotCsvs.get(i + 1).toString());
                String nextTime = nextRow[0].split(" - ")[1].trim();
                String nextCourseTime = timeParser.padZeroToHourAndMinute(nextTime).trim();
                if (nextCourseTime.trim().equals("07:30:00")) {
                    duration = timeParser.timeDifferenceInHours(endTime, courseTime);
                } else {
                    duration = timeParser.timeDifferenceInHours(nextCourseTime, courseTime);
                }
            }

            Timeslot timeslot = new Timeslot(i, row[0], duration);
            timeslots.add(timeslot);
        }
    }

    public Instructor findInstructorById(int id) {
        return instructors.stream().filter(instructor -> instructor.getId() == id).findFirst()
                .orElse(null);
    }

    public Course findCourseById(int id) {
        return courses.stream().filter(course -> course.getId() == id).findFirst().orElse(null);
    }

    public Timeslot findTimeslotById(int id) {
        return timeslots.stream().filter(timeslot -> timeslot.getId() == id).findFirst().orElse(null);
    }

    public Group findGroupById(int id) {
        return groups.stream().filter(group -> group.getId() == id).findFirst().orElse(null);
    }

    public void setNumberOfClasses() {
        numberOfClasses = courses.size();
    }

    public void setData() {
        setRooms();
        setInstructors();
        setTimeslots();
        setGroups();
        setCourses();
        setNumberOfClasses();
    }

    public static void main(String[] args) {
        Data data = new Data();
        data.setData();
    }
}
