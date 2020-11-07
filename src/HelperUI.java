import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.controlsfx.control.HyperlinkLabel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class HelperUI extends Application {

    private OptionsUtil optionsUtil;
    private TrayIcon trayIcon;
    private TabPane tabPane;
    private ArrayList<TextInputControl> textInputs;
    private boolean launchReadError = false;

    @Override
    public void start(Stage primaryStage) {

        optionsUtil = new OptionsUtil();
        // start tray icon for notifications
        try {
            BufferedImage img = ImageIO.read(Helper.class.getResource("res/cog.png"));
            trayIcon = new TrayIcon(
                    img.getScaledInstance(SystemTray.getSystemTray().getTrayIconSize().width, -1,
                    java.awt.Image.SCALE_SMOOTH), "Meeting Helper UI");
            SystemTray.getSystemTray().add(trayIcon);
            primaryStage.setOnCloseRequest(e -> SystemTray.getSystemTray().remove(trayIcon));
        } catch (IOException | AWTException e) { e.printStackTrace(); }

        System.out.println(getParameters().getRaw());

        // which tab to show
        try {
            optionsUtil.updateOptionsVars();
            tabPane = new TabPane(setupToday(), setupOptions(), setupAbout());
            if (getParameters().getRaw().size() > 0) {
                switch (getParameters().getRaw().get(0)) {
                    case "today": tabPane.getSelectionModel().select(0); break;
                    case "options": tabPane.getSelectionModel().select(1); break;
                    case "about": tabPane.getSelectionModel().select(2); break;
                }
            }
        } catch (Exception ex) { // options read error mode, show options tab with error txt
            ex.printStackTrace(); launchReadError = true;
            tabPane = new TabPane(setupOptions(), setupAbout());
        }

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 750, 590);
        primaryStage.setScene(scene);
        primaryStage.setTitle("MeetingHelper");
        primaryStage.getIcons().add(new Image("res/icon.png"));
        primaryStage.show();
    }

    public Tab setupToday() {

        Pane pane = new Pane();

        Text welcome = new Text(10, 40, "Today View"); welcome.setFont(Font.font(30));
        Text schedule = new Text(40, 70, "Your Schedule"); schedule.setFont(Font.font(20));
        pane.getChildren().addAll(welcome, schedule);

        int i = 100;
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        if (!(day.getValue() == 6 || day.getValue() == 7)) {
            for (Pair<Integer, LocalTime> periodStart : optionsUtil.schedule.get(day.getValue() - 1)) {
                String txt = "Period " + periodStart.getKey() + ": " + periodStart.getValue();

                Text periodTxt = new Text(70, i, txt); periodTxt.setFont(Font.font(15));
                Button open = new Button("Open link manually");
                open.setLayoutX(190); open.setLayoutY(i - 20);
                open.setOnAction(e -> {
                    boolean hasAlt = false;
                    for (Triplet<Integer, DayOfWeek, String> altLink : optionsUtil.altLinks) {
                        if (periodStart.getKey().equals(altLink.getV1()) && altLink.getV2().equals(day)) {
                            getHostServices().showDocument(altLink.getV3());
                            hasAlt = true;
                        }
                    }
                    if (!hasAlt) {
                        try {
                            getHostServices().showDocument(optionsUtil.links.get(periodStart.getKey() - 1));
                        } catch (IndexOutOfBoundsException ex) {
                            notify("Error: Could not find period link, check options", 3);
                        }
                    }
                });
                pane.getChildren().addAll(periodTxt, open);
                i += 30;
            }
        } else {
            Text periodTxt = new Text(70, i, "Nothing Today!"); periodTxt.setFont(Font.font(15));
            pane.getChildren().add(periodTxt); i += 20;
        }

        i += 10;
        Text alerts = new Text(40, i, "Your Alerts"); alerts.setFont(Font.font(20));
        pane.getChildren().add(alerts); i += 30;
        int count = 0;
        for (Triplet<DayOfWeek, LocalTime, String> alert : optionsUtil.alerts) {
            if (alert.getV1().equals(day)) {
                String txt = alert.getV2() + ": " + alert.getV3();
                Text alertTxt = new Text(70, i, txt); alertTxt.setFont(Font.font(15));
                pane.getChildren().add(alertTxt); count++; i += 30;
            }
        }
        if (count == 0) {
            Text alertTxt = new Text(70, i, "No alerts today!"); alertTxt.setFont(Font.font(15));
            pane.getChildren().add(alertTxt); i += 30;
        }

        i += 10;
        Text other = new Text(40, i, "All Links"); other.setFont(Font.font(20));
        pane.getChildren().add(other); i += 20;
        for (int j = 0; j < optionsUtil.links.size(); j++) {
            Button open = new Button("Period " + (j+1));
            open.setLayoutX((j+1)*70); open.setLayoutY(i);
            int finalJ = j;
            open.setOnAction(e -> getHostServices().showDocument(optionsUtil.links.get(finalJ)));
            pane.getChildren().add(open);
        }
        i += 35; int count2 = 0;
        for (Triplet<Integer, DayOfWeek, String> altLink : optionsUtil.altLinks) {
            Button open = new Button("Period " + altLink.getV1() + " Alt");
            open.setLayoutX((count2*90)+70); open.setLayoutY(i);
            open.setOnAction(e -> getHostServices().showDocument(altLink.getV3()));
            pane.getChildren().add(open); count2++;
        }

        return new Tab("Today", pane);
    }

    public Tab setupOptions() {

        Group group = new Group();

        Text welcome = new Text(10, 20, "MeetingHelper Options");
        Text in1 = new Text(40, 50, "All fields marked with a * are required");
        Text in2 = new Text(40, 75, "Make sure to use the correct format!");
        welcome.setFont(Font.font(30)); in1.setFont(Font.font(20)); in2.setFont(Font.font(20));

        Text error = new Text(40, 95,
                "Error: Loop is not running, check options format, exit, and relaunch program");
        error.setFont(Font.font("System", FontWeight.BOLD, 17));
        error.setFill(Color.RED); error.setVisible(launchReadError);

        HBox parentBox = new HBox(10); parentBox.setLayoutY(100);
        group.getChildren().addAll(welcome, in1, in2, error, parentBox);

        VBox box1 = new VBox(5); box1.setPadding(new Insets(5,5,5,10));
        VBox box2 = new VBox(5); box2.setPadding(new Insets(5,10,5,5));
        parentBox.getChildren().add(box1); parentBox.getChildren().add(box2);

        textInputs = new ArrayList<>();

        Label scheduleLb = quickLabel("*Schedule (in 24 hour time)");
        Label scheduleLb2 = quickLabel("Format: <period#> <startTime>");
        box1.getChildren().addAll(scheduleLb, scheduleLb2);
        for (int i = 1; i <= 5; i++) {
            String name = DayOfWeek.of(i).name();
            Label nameLb = new Label("*" + name.charAt(0) + name.toLowerCase().substring(1));
            TextArea scheduleField = new TextArea(); scheduleField.setPrefSize(175, 130);
            box1.getChildren().addAll(nameLb, scheduleField);
            textInputs.add(scheduleField);
        }

        Label linkLb = quickLabel("*Default Meeting Links- \"https://\" required (put in period order)");
        TextArea linkField = new TextArea(); linkField.setPrefHeight(170);
        textInputs.add(linkField);

        Label altLinkLb1 = quickLabel("Alternate Links (days when teachers have different links)");
        Label altLinkLb2 = quickLabel("Format: <period#> <day of week> <link>");
        TextArea altLinkField = new TextArea(); altLinkField.setPrefHeight(100);
        textInputs.add(altLinkField);

        Label openLb = quickLabel("*Minutes Before (minutes before class the link is opened)");
        TextField openField = new TextField(); textInputs.add(openField);

        Label alertLb1 = quickLabel("General Alerts (send yourself reminders)");
        Label alertLb2 = quickLabel("Format: <day of week> <time> \"<text>\"");
        TextArea alertField = new TextArea(); alertField.setPrefHeight(120);
        textInputs.add(alertField);

        Button save = new Button("Save Options");
        save.setOnAction(e -> {
            try {
                optionsUtil.writeOptions(textInputs);
                optionsUtil.updateOptionsVars();
                if (optionsUtil.linkCheck() != optionsUtil.links.size()) {
                    String txt = "Error: Number of links (" + optionsUtil.links.size() +
                            ") does not match number of periods (" + optionsUtil.linkCheck() + ")";
                    notify(txt, 3);
                    throw new RuntimeException(txt);
                }
                notify("Options have been saved", 1);
                if (!launchReadError) {
                    tabPane.getTabs().set(0, setupToday());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                notify("Error: Could not parse options, check options format, required fields", 3);
            }
        });

        box2.getChildren().addAll(linkLb, linkField, altLinkLb1, altLinkLb2, altLinkField,
                openLb, openField, alertLb1, alertLb2, alertField, save);

        try {
            optionsUtil.fillOptionsText(textInputs);
        } catch (Exception ex) {
            ex.printStackTrace(); error.setVisible(true);
        }

        ScrollPane scrollPane = new ScrollPane(group);
        scrollPane.pannableProperty().set(true);
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);

        return new Tab("Options", scrollPane);
    }

    public Tab setupAbout() {

        Pane pane = new Pane();

        ImageView icon = new ImageView(new Image(
                "res/icon.png", 150, 150, true, true));
        icon.setX(20); icon.setY(5);
        Text welcome = new Text(20, 175, "About MeetingHelper"); welcome.setFont(Font.font(30));
        Text version = new Text(60, 205, "Version " + Helper.version); version.setFont(Font.font(20));
        Text about = new Text(60, 235, "Developed by Jonathan Ma"); about.setFont(Font.font(20));
        HyperlinkLabel gitLink = new HyperlinkLabel(
                "Help/GitHub Repository Link: [https://github.com/jonathanjma/MeetingHelper]");
        gitLink.setLayoutX(60); gitLink.setLayoutY(245); gitLink.setStyle("-fx-font-size: 15;");
        gitLink.setOnAction(e ->
                getHostServices().showDocument("https://github.com/jonathanjma/MeetingHelper"));
        Button check = new Button("Check for Updates");
        check.setLayoutX(60); check.setLayoutY(275);
        HyperlinkLabel checkResult = new HyperlinkLabel();
        checkResult.setLayoutX(60); checkResult.setLayoutY(305);
        check.setOnAction(e -> {
            ArrayList<String> content = new ArrayList<>();
            try {
                URL url = new URL("https://my-json-server.typicode.com/jonathanjma/MeetingHelper/data");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET"); con.setConnectTimeout(5000); con.setReadTimeout(5000);
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) { content.add(inputLine); }
                in.close(); con.disconnect();
                double latestVersion = Double.parseDouble(content.get(2).split(": ")[1]);
                System.out.println("Latest Version: " + latestVersion);
                if (latestVersion > Helper.version) {
                    checkResult.setText("Version " + latestVersion + " is available, download at " +
                            "[https://github.com/jonathanjma/MeetingHelper/releases/latest]");
                    checkResult.setOnAction(e2 -> getHostServices()
                            .showDocument("https://github.com/jonathanjma/MeetingHelper/releases/latest"));
                } else {
                    checkResult.setText("You are using the latest version");
                }
                checkResult.setStyle("-fx-font-size: 15; -fx-font-weight: normal");
            } catch (Exception ex) {
                ex.printStackTrace(); checkResult.setStyle("-fx-font-size: 15; -fx-font-weight: bold");
                checkResult.setText("Update check failed, check your connection and try again");
            }
        });
        pane.getChildren().addAll(icon, welcome, version, about, gitLink, check, checkResult);

        return new Tab("About", pane);
    }

    public Label quickLabel(String txt) {
        Label lbl = new Label(txt); lbl.setFont(Font.font(15)); return lbl;
    }

    public void notify(String text, int level) {
        TrayIcon.MessageType type = TrayIcon.MessageType.NONE;
        switch (level) {
            case 0: type = TrayIcon.MessageType.NONE; break;
            case 1: type = TrayIcon.MessageType.INFO; break;
            case 2: type = TrayIcon.MessageType.WARNING; break;
            case 3: type = TrayIcon.MessageType.ERROR; break;
        }
        trayIcon.displayMessage("Meeting Helper :-)", text, type);
    }
}
