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
     *
     * @param arrayOfString
     * @return list
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

    public List uniqueList(List listWithDuplicates) {
        List<Integer> listWithoutDuplicates = new ArrayList<>(
                new HashSet<>(listWithDuplicates));
        return listWithoutDuplicates;
    }
}
