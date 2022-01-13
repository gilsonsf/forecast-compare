package com.gsf.forecastscompare.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

    private static String FORMAT_RAW_DATE = "dd/MM/yyyy";
    private static String FORMAT_BI_DATE = "yyyy-MM-dd";


    public static Date formatterRawFileDate(String date) {
        try {
            return new SimpleDateFormat(FORMAT_RAW_DATE).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date formatterBIFileDate(String date) {
        try {
            return new SimpleDateFormat(FORMAT_BI_DATE).parse(date.split(" ")[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String dateFormatted(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(FORMAT_RAW_DATE);
        return dateFormat.format(date.getTime());
    }

//  public static void main(String[] args) throws ParseException {
//      Date date = formatterRawFileDate("01/11/2019");
//
//      dateFormatted(date);
//
//  }
}
