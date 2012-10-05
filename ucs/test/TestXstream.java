import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.cloud.ucs.manager.UcsBlade;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class TestXstream {

    @Test
    public void test() throws IOException {
        XStream xs = new XStream(new DomDriver());
        InputStream is = TestXstream.class.getClassLoader().getResourceAsStream("computetest.txt");
        String xml = IOUtils.toString(is);
        System.out.println(xml);
        //xs.alias("computeBlade", computeBlade.class);
        //computeBlade c = (computeBlade) xs.fromXML(is);
        //System.out.print(c.getDn());
        
        Pattern p = Pattern.compile("dn=(\".+?\")");
        Matcher m = p.matcher(xml);
        m.find();
        System.out.println(m.group(0));
        
        p = Pattern.compile("<computeBlade (.*)/>");
        m = p.matcher(xml);
        m.find();
        System.out.println(m.groupCount());
        
        List<UcsBlade> blades = UcsBlade.valueOf(xml);
        for (UcsBlade b : blades) {
            System.out.println(b.getDn());
        }
    }
}
