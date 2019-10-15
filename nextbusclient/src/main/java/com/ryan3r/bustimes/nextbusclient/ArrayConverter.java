package com.ryan3r.bustimes.nextbusclient;

import androidx.room.TypeConverter;

public class ArrayConverter {
    // convert a list to a json string
    @TypeConverter
    public static String toString(String[] times) {
        StringBuilder raw = new StringBuilder();

        for(String time : times) {
            if(raw.length() > 0) raw.append(",");
            raw.append(time);
        }

        return raw.toString();
    }

    // convert a json string to a list
    @TypeConverter
    public static String[] toRoute(String raw) {
        return raw.split(",");
    }
}
