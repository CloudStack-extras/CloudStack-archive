package com.cloud.ucs.manager;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlFieldHelper {
    public static String getField(String xmlDoc, String fieldName) {
        Pattern p = Pattern.compile(String.format("%s=(\".+?\")", fieldName));
        Matcher m = p.matcher(xmlDoc);
        m.find();
        String val = m.group(0);
        String[] tuples = val.split("=");
        if (tuples[0].equals(fieldName)) {
            return tuples[1].replace("\"", "");
        } else {
            return tuples[0].replace("\"", "");
        }
    }

    public static String replaceTokens(String text, Map<String, String> replacements) {
        Pattern pattern = Pattern.compile("\\[(.+?)\\]");
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String replacement = replacements.get(matcher.group(1));
            if (replacement != null) {
                matcher.appendReplacement(buffer, "");
                buffer.append(replacement);
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
