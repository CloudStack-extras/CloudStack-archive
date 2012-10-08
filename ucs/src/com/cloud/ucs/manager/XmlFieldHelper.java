package com.cloud.ucs.manager;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

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
    
    public static String formatXml(String xmlDoc) {
        try {
            final Document document = parseXmlFile(xmlDoc);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    

    private static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
