package com.suprememedicator.suprememedicator.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static String format(String template, Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Args must be a sequence of key-value pairs.");
        }

        Map<String, String> parameters = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof String)) {
                throw new IllegalArgumentException("Key argument must be a string.");
            }

            parameters.put((String) args[i], args[i + 1].toString());
        }

        return format(template, parameters);
    }

    public static String format(String template, Map<String, String> parameters) {
        StringBuilder newTemplate = new StringBuilder(template);
        List<String> valueList = new ArrayList<>();

        Matcher matcher = Pattern.compile("[$][{](\\w+)}").matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);

            String paramName = "${" + key + "}";
            int index = newTemplate.indexOf(paramName);
            if (index != -1) {
                newTemplate.replace(index, index + paramName.length(), "%s");
                valueList.add(parameters.get(key));
            }
        }

        return String.format(newTemplate.toString(), valueList.toArray());
    }
}