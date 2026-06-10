package com.example.project.audit;

public class ResultSerializer {

    public static String safeSerialize(Object obj, int maxLength) {
        if (obj == null) return null;
        String text = String.valueOf(obj);
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        }
        return text;
    }
}