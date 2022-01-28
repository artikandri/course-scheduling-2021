package com.course_scheduling.assets;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author @artikandri
 */
public class ListParser {

    /**
     * convert array of number in strings to list of integers
     *
     * @param array of string
     * @return list of integers
     */
    public List<Integer> arrayOfStringToListOfIntegers(String[] arrayOfString) {
        List list = Arrays.asList();
        if (!ArrayUtils.isEmpty(arrayOfString)) {
            list = Arrays.asList(arrayOfString)
                    .stream()
                    .filter(item -> item != null && !"".equals(item) && !" ".equals(item))
                    .map(pref -> Integer.parseInt(pref.trim()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    /**
     * convert array of number in strings to list of doubles
     *
     * @param array of string
     * @return list of doubles
     */
    public List<Double> arrayOfStringToListOfDoubles(String[] arrayOfString) {
        List list = Arrays.asList();
        if (!ArrayUtils.isEmpty(arrayOfString)) {
            list = Arrays.asList(arrayOfString)
                    .stream()
                    .filter(item -> item != null && !"".equals(item) && !" ".equals(item))
                    .map(pref -> Double.parseDouble(pref.trim()))
                    .collect(Collectors.toList());
        }
        return list;
    }

    /**
     * remove duplicates from a list
     *
     * @param list of object
     * @return unique list without duplicates
     */
    public List uniqueList(List listWithDuplicates) {
        List<Integer> listWithoutDuplicates = new ArrayList<>(
                new HashSet<>(listWithDuplicates));
        return listWithoutDuplicates;
    }
}
