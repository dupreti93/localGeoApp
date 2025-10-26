package com.localapp.util;

import java.time.Instant;

public class ParseUtil {

    public static String str(Object o) {
        return o != null ? o.toString() : "";
    }

    public static Double dbl(Object o) {
        if (o == null) return null;
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return null; }
    }

    public static Instant parseTime(String s) {
        try { return Instant.parse(s != null ? s : "1970-01-01T00:00:00Z"); }
        catch (Exception e) { return Instant.EPOCH; }
    }
}

