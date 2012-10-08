package com.cloud.ucs.manager;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;

import com.cloud.ucs.schema.computeBlade.ComputeBlade;
import com.cloud.ucs.schema.computeBlade.ConfigResolveClass;
import com.cloud.utils.exception.CloudRuntimeException;

public class UcsBlade {
    private String dn;

    public UcsBlade(String dn) {
        this.dn = dn;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public static List<UcsBlade> valueOf(String wholeDoc) {
        try {
            List<UcsBlade> blades = new ArrayList<UcsBlade>();
            JAXBContext context = JAXBContext.newInstance("com.cloud.ucs.schema.computeBlade");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            ConfigResolveClass cc = (ConfigResolveClass) unmarshaller.unmarshal(new StringReader(wholeDoc));
            for (ComputeBlade cb : cc.getOutConfigs().getComputeBlade()) {
                UcsBlade blade = new UcsBlade(cb.getDn());
                blades.add(blade);
            }
            
            /*
            Pattern p = Pattern.compile("<computeBlade (.*)/>");
            Matcher m = p.matcher(wholeDoc);
            while (m.find()) {
                UcsBlade blade = new UcsBlade(m.group());
                blades.add(blade);
            }
            */
            return blades;
        } catch (Exception e) {
            throw new CloudRuntimeException(e.getMessage(), e);
        }
    }
}
