package ru.kirill.checksfirstpage.util;

/**
 * Created by K on 25.06.13.
 */
public class Helper {
    static private String[] monthNames = {"", "€нварь", "февраль", "март", "апрель", "май", "июнь", "июль", "август", "сент€брь", "окт€брь", "но€брь", "декабрь"};
    static public String getMonthName(int monthNumber) {
        return monthNames[monthNumber] + " " + monthNumber;
    }
}
