package settings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Settings {
    public static final Properties properties = getProperties();

    private Settings() {}

    private static Properties getProperties() {
        Properties properties = new Properties();
        try {
            File xmlFile = new File("settings.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new FileInputStream(xmlFile));
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("property");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                String name = element.getAttribute("name");
                String value = element.getTextContent();
                properties.setProperty(name, value);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }
}
