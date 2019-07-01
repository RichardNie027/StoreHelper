package com.nec.utils;

public class StringUtil {

    public static String left(String source, int length) {
        int len = source.length();
        return source.substring(0, length > len ? len : length);
    }

    public static String right(String source, int length) {
        int len = source.length();
        return source.substring(length >= len ? 0 : len - length);
    }

    public static String trim(String source) {
        return source.trim();
    }

    public static String trimLeft(String source) {
        if(source.isEmpty())
            return "";
        for(int i=0; i < source.length(); i++) {
            char c = source.charAt(i);
            if(c > 32)
                return source.substring(i);
        }
        return "";
    }

    public static String trimRight(String source) {
        if(source.isEmpty())
            return "";
        for(int i=source.length()-1; i >= 0; i--) {
            char c = source.charAt(i);
            if(c > 32)
                return source.substring(0, i+1);
        }
        return "";
    }
}
