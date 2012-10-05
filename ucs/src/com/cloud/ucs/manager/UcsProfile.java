package com.cloud.ucs.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UcsProfile {
    private String dn;

    public UcsProfile(String xml) {
        dn = XmlFieldHelper.getField(xml, "value");
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }
    
    public static List<UcsProfile> valueOf(String wholeDoc) {
        List<UcsProfile> profiles = new ArrayList<UcsProfile>();
        Pattern p = Pattern.compile("<dn (.*)/>");
        Matcher m = p.matcher(wholeDoc);
        while (m.find()) {
            UcsProfile blade = new UcsProfile(m.group(0));
            profiles.add(blade);
        }
        return profiles;
    }
}
