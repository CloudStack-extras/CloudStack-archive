package com.cloud.ucs.manager;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.cloud.ucs.schema.computeBlade.ComputeBlade;
import com.cloud.ucs.schema.computeBlade.ConfigResolveClass;
import com.cloud.ucs.schema.listProfile.ConfigFindDnsByClassId;
import com.cloud.ucs.schema.listProfile.Dn;
import com.cloud.utils.exception.CloudRuntimeException;

public class UcsProfile {
    private String dn;

    public UcsProfile(String dn) {
        this.dn = dn;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public static List<UcsProfile> valueOf(String wholeDoc) {
        try {
            List<UcsProfile> profiles = new ArrayList<UcsProfile>();
            JAXBContext context = JAXBContext.newInstance("com.cloud.ucs.schema.listProfile");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            ConfigFindDnsByClassId cc = (ConfigFindDnsByClassId) unmarshaller.unmarshal(new StringReader(wholeDoc));
            for (Dn dn : cc.getOutDns().getDn()) {
                UcsProfile p = new UcsProfile(dn.getValue());
                profiles.add(p);
            }
            return profiles;
        } catch (Exception el) {
            throw new CloudRuntimeException(el.getMessage(), el);
        }
    }
}
