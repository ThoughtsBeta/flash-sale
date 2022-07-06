package com.actionworks.flashsale.util;

public class StringUtil {
    public static String link(Object... items) {
        if (items == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            stringBuilder.append(items[i]);
            if (i < items.length - 1) {
                stringBuilder.append("_");
            }
        }
        return stringBuilder.toString();
    }
}
