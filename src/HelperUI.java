import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.File;
import java.time.DayOfWeek;
import java.util.ArrayList;

public class HelperUI extends Application {

    public final static String path = System.getProperty("user.home")+"/helper_options.xml";
    private static OptionsUtil optionsUtil;
    private static Stage staticStageRef;
    private static boolean hidden;
    private static ArrayList<TextInputControl> textInputs;
    private static Text errorText;

    @Override
    public void start(Stage primaryStage) {

        optionsUtil = new OptionsUtil();

        Text welcome = new Text(10, 20, "MeetingHelper Options");
        Text in1 = new Text(40, 50, "All fields marked with a * are required");
        Text in2 = new Text(40, 75, "Make sure to use the correct format!");
        welcome.setFont(Font.font(30)); in1.setFont(Font.font(20)); in2.setFont(Font.font(20));

        Text error = new Text(40, 95, "Error: Loop is not running, check options format and relaunch program");
        error.setFont(Font.font(15)); error.setFill(Color.RED); error.setVisible(false); errorText = error;

        HBox parentBox = new HBox(10); parentBox.setLayoutY(100);
        Group group = new Group();
        group.getChildren().addAll(welcome, in1, in2, parentBox, error);

        VBox box1 = new VBox(5); box1.setPadding(new Insets(5,5,5,10));
        VBox box2 = new VBox(5); box2.setPadding(new Insets(5,10,5,5));
        parentBox.getChildren().add(box1); parentBox.getChildren().add(box2);

        textInputs = new ArrayList<>();

        Label scheduleLb = new Label("*Schedule (in 24 hour time)");
        Label scheduleLb2 = new Label("Format: <period#> <startTime>");
        scheduleLb.setFont(Font.font(15)); scheduleLb2.setFont(Font.font(15));
        box1.getChildren().addAll(scheduleLb, scheduleLb2);
        for (int i = 1; i <= 5; i++) {
            String name = DayOfWeek.of(i).name();
            Label nameLb = new Label("*" + name.charAt(0) + name.toLowerCase().substring(1));
            TextArea scheduleField = new TextArea(); scheduleField.setPrefSize(175, 130);
            box1.getChildren().addAll(nameLb, scheduleField);
            textInputs.add(scheduleField);
        }

        Label linkLb = new Label("*Default Meeting Links (put in period order)");
        linkLb.setFont(Font.font(15));
        TextArea linkField = new TextArea(); linkField.setPrefHeight(170);
        textInputs.add(linkField);

        Label altLinkLb1 = new Label("Alternate Links (days when teachers have different links)");
        Label altLinkLb2 = new Label("Format: <period#> <day of week> <link>");
        altLinkLb1.setFont(Font.font(15)); altLinkLb2.setFont(Font.font(15));
        TextArea altLinkField = new TextArea(); altLinkField.setPrefHeight(100);
        textInputs.add(altLinkField);

        Label openLb = new Label("*Minutes Before (minutes before class the link is opened)");
        openLb.setFont(Font.font(15));
        TextField openField = new TextField();
        textInputs.add(openField);

        Label alertLb1 = new Label("General Alerts (send yourself reminders)");
        Label alertLb2 = new Label("Format: <day of week> <time> \"<text>\"");
        alertLb1.setFont(Font.font(15)); alertLb2.setFont(Font.font(15));
        TextArea alertField = new TextArea(); alertField.setPrefHeight(120);
        textInputs.add(alertField);

        Button save = new Button("Save Options");
        save.setOnAction(e -> {
            try {
                optionsUtil.updateOptionsVars(textInputs);
                optionsUtil.writeOptions(textInputs);
                notify("Options have been saved", 0);
            } catch (Exception ex) {
                ex.printStackTrace();
                notify("Error: Could not parse options, check options format, required fields", 3);
            }
        });

        box2.getChildren().addAll(linkLb, linkField, altLinkLb1, altLinkLb2, altLinkField,
                openLb, openField, alertLb1, alertLb2, alertField, save);

        try {
            if (!new File(path).exists()) {
                optionsUtil.createOptions();
            }
            OptionsUtil.fillOptionsText(textInputs);
            optionsUtil.updateOptionsVars(textInputs);
        } catch (Exception ex) {
            ex.printStackTrace();
            error.setVisible(true);
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(group);
        scrollPane.pannableProperty().set(true);
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: rgb(255,255,255);");

        Scene scene = new Scene(scrollPane, 750, 590);
        primaryStage.setScene(scene);
        primaryStage.setTitle("MeetingHelper Options");
        primaryStage.getIcons().add(new Image("icon.png"));
        primaryStage.show();

        HelperUI.staticStageRef = primaryStage;
        Platform.setImplicitExit(false);

        primaryStage.setOnCloseRequest(e -> {
            primaryStage.hide(); hidden = true;
            new Thread(() -> {
                try { Thread.sleep(5000); } catch (InterruptedException ex) { ex.printStackTrace(); }
                System.gc();
            });
        });
    }

    public static void notify(String text, int level) {
        if (hidden) {
            Stage stage = new Stage(); stage.setScene(new Scene(new Pane(), 0.1,0.1));
            stage.initStyle(StageStyle.TRANSPARENT); stage.setIconified(true); stage.show();

            new Thread(() -> {
                try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }
                Platform.runLater(stage::hide);
            }, "notification_term").start();
        }

        Notifications n = Notifications.create()
                .title("Meeting Helper :-)").text(text).hideAfter(Duration.seconds(10));
        switch (level) {
            case 0 : n.showInformation(); break; case 1 : n.showConfirm(); break;
            case 2 : n.showWarning(); break; case 3 : n.showError(); break;
        }
        new AudioClip(HelperUI.class.getResource("notification_sound.mp3").toExternalForm()).play();
    }

    public static OptionsUtil optionsUtil() { return optionsUtil; }

    public static void show() {
        if (hidden) {
            Platform.runLater(() -> {
                staticStageRef.show();
                try {
                    OptionsUtil.fillOptionsText(textInputs);
                    optionsUtil.updateOptionsVars(textInputs);
                } catch (Exception ex) {
                    ex.printStackTrace(); errorText.setVisible(true);
                }
            });
            hidden = false;
        }
    }
}
