/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.course_scheduling.assets;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author hp
 */
public class ListParser {

    /**
     *
     * @param arrayOfString
     * @return
     */
    // Example
    //        String[] numbers = {"1", "2"};
    //        ListParser listParser = new ListParser();
    //        List arr = listParser.arrayOfStringToListOfIntegers(numbers);
    public List arrayOfStringToListOfIntegers(String[] arrayOfString) {
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

    public static void main(String[] args) {

    }
}
