package com.expectlost;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static Date get(String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return simpleDateFormat.parse(pattern);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
