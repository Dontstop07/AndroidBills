package ru.kirill.checksfirstpage.util;

/**
 * Created by K on 25.06.13.
 */
public class Helper {
    static private String[] monthNames = {"", "������", "�������", "����", "������", "���", "����", "����", "������", "��������", "�������", "������", "�������"};
    static public String getMonthName(int monthNumber) {
        return monthNames[monthNumber] + " " + monthNumber;
    }
}
