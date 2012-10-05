package com.cloud.ucs.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UcsBlade {
    private String dn;

    public UcsBlade(String xmlDoc) {
        dn = XmlFieldHelper.getField(xmlDoc, "dn");
    }
    
    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }
    
    public static List<UcsBlade> valueOf(String wholeDoc) {
        List<UcsBlade> blades = new ArrayList<UcsBlade>();
        Pattern p = Pattern.compile("<computeBlade (.*)/>");
        Matcher m = p.matcher(wholeDoc);
        while (m.find()) {
            UcsBlade blade = new UcsBlade(m.group(0));
            blades.add(blade);
        }
        return blades;
    }
}
