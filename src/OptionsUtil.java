import javafx.scene.control.TextInputControl;
import javafx.util.Pair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

public class OptionsUtil {

    // option variables
    public ArrayList<ArrayList<Pair<Integer, LocalTime>>> schedule = new ArrayList<>();
    public ArrayList<String> links = new ArrayList<>();
    public ArrayList<Triplet<Integer, DayOfWeek, String>> altLinks = new ArrayList<>();
    public int earlyMin;
    public ArrayList<Triplet<DayOfWeek, LocalTime, String>> alerts = new ArrayList<>();

    // update option variables only
    public void updateOptionsVars(ArrayList<TextInputControl> options) {
        schedule.clear(); links.clear(); altLinks.clear(); alerts.clear();

        // schedule
        for (int i = 0; i < 5; i++) {
            String[] arr = options.get(i).getText().split("\n");
            ArrayList<Pair<Integer, LocalTime>> day = new ArrayList<>();
            for (String str : arr) {
                String[] split = str.split(" ");
                day.add(new Pair<>(Integer.parseInt(split[0]),
                        LocalTime.of(Integer.parseInt(split[1].split(":")[0]),
                                     Integer.parseInt(str.split(":")[1]))));
            }
            schedule.add(day);
        }
        // links
        String[] arr1 = options.get(5).getText().split("\n");
        if (arr1[0].length() != 0) {
            links.addAll(Arrays.asList(options.get(5).getText().split("\n")));
        } else { // throw error if no links
            throw new RuntimeException("no links");
        }
        // alt links
        String[] arr2 = options.get(6).getText().split("\n");
        if (arr2[0].length() != 0) {
            for (String str : arr2) {
                String[] split = str.split(" ");
                altLinks.add(new Triplet<>(Integer.parseInt(split[0]),
                        DayOfWeek.valueOf(split[1].toUpperCase()), split[2]));
            }
        }
        // open before time
        earlyMin = Integer.parseInt(options.get(7).getText());
        // alerts
        String[] arr3 = options.get(8).getText().split("\n");
        if (arr3[0].length() != 0) {
            for (String str : arr3) {
                String[] split = str.split(" ");
                alerts.add(new Triplet<>(DayOfWeek.valueOf(split[0].toUpperCase()),
                        LocalTime.of(Integer.parseInt(split[1].split(":")[0]),
                                Integer.parseInt(split[1].split(":")[1])),
                        str.split("\"")[1]));
            }
        }

        System.out.println(schedule + "\n" + links + "\n" + altLinks + "\n" + earlyMin + "\n" + alerts);
    }

    // write to options file only
    public void writeOptions(ArrayList<TextInputControl> options) throws JDOMException, IOException {
        Document document = new SAXBuilder().build(new File(HelperUI.path));
        for (int i = 0; i < options.size(); i++) {
            document.getRootElement().getChildren().get(i).setText(options.get(i).getText());
        }
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        output.output(document, new FileOutputStream(HelperUI.path));
    }

    // fill input boxes with options file content
    public static void fillOptionsText(ArrayList<TextInputControl> options)
            throws JDOMException, IOException {
        Document document = new SAXBuilder().build(new File(HelperUI.path));
        for (int i = 0; i < options.size(); i++) {
            options.get(i).setText(document.getRootElement().getChildren().get(i).getText());
        }
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        output.output(document, new FileOutputStream(HelperUI.path));
    }

    // create new options file
    public void createOptions() throws IOException {
        Document document = new Document();
        document.setRootElement(new Element("options"));
        for (int i = 1; i <= 5; i++) {
            Element day = new Element(DayOfWeek.of(i).toString().toLowerCase());
            switch (i) {
                case 1 : day.setText("1 8:00\n2 8:50\n3 10:45\n4 11:35\n5 12:55\n6 1:45"); break;
                case 2 : case 4 : day.setText("1 8:00\n3 10:45\n5 12:55\n"); break;
                case 3 : case 5 : day.setText("2 8:00\n4 10:45\n6 12:55"); break;
            }
            document.getRootElement().addContent(day);
        }
        Element links = new Element("links"); links.setText("zoom.us/test");
        document.getRootElement().addContent(links);
        Element altLinks = new Element("altLinks");
        document.getRootElement().addContent(altLinks);
        Element earlyOpen = new Element("earlyOpen"); earlyOpen.setText("3");
        document.getRootElement().addContent(earlyOpen);
        Element otherAlerts = new Element("otherAlerts");
        document.getRootElement().addContent(otherAlerts);

        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        output.output(document, new FileOutputStream(HelperUI.path));
    }
}
