package com.localapp.util;

import java.util.Map;

public class EventFilterUtil {

    public static boolean matchMood(Map<String, Object> e, String mood) {
        if (mood == null || mood.isEmpty()) return true;
        String name = ParseUtil.str(e.get("name")).toLowerCase();
        String m = mood.toLowerCase();
        if (m.contains("chill")) return name.contains("jazz") || name.contains("acoustic") || name.contains("open mic");
        if (m.contains("loud")) return name.contains("karaoke") || name.contains("bar") || name.contains("dj") || name.contains("trivia");
        if (m.contains("date")) return name.contains("jazz") || name.contains("dinner") || name.contains("live");
        return true;
    }
}

