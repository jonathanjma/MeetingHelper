import javafx.application.Application;
import javafx.application.Platform;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Helper {

    public static void main(String[] args) throws InterruptedException {

        new Thread(() -> Application.launch(HelperUI.class), "javafx").start();
        Thread.sleep(1000);

        // set up tray icon
        try {
            SystemTray tray = SystemTray.getSystemTray();
            BufferedImage img = ImageIO.read(Helper.class.getResource("tray.png"));
            int trayIconWidth = tray.getTrayIconSize().width;
            TrayIcon trayIcon = new TrayIcon(img.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH),
                    "Meeting Helper :-)");

            PopupMenu popup = new PopupMenu();
            MenuItem optionsItem = new MenuItem("Options");
            MenuItem exitItem = new MenuItem("Exit");
            popup.add(optionsItem); popup.addSeparator(); popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            optionsItem.addActionListener(e -> HelperUI.show());

            exitItem.addActionListener(e -> {
                tray.remove(trayIcon); notify("Exiting", 0);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) { ex.printStackTrace(); }
                System.exit(0);
            });

            tray.add(trayIcon);
        } catch (IOException | AWTException e) {
            e.printStackTrace();
            notify("Tray Error, Exiting", 3); Thread.sleep(3000);
            System.exit(-1);
        }

        OptionsUtil options = HelperUI.optionsUtil();
        boolean first = true;

        /*LocalDateTime start = LocalDateTime.of(2020,8,31,7,0);
        int count = 0;*/

        //noinspection InfiniteLoopStatement
        while (true) {

            LocalDateTime now = LocalDateTime.now(); //start.plusMinutes(count);
            DayOfWeek day = now.getDayOfWeek();
            //System.out.println(day.name() + " " + now);

            if (day.getValue() >= 1 && day.getValue() <= 5) { // if not weekends

                // add open early time
                LocalTime time = LocalTime.of(now.getHour(), now.getMinute()).plusMinutes(options.earlyMin);

                // check schedule to check if time to open link
                for (Pair<Integer, LocalTime> periodStart : options.schedule.get(day.getValue()-1)) {

                    if (time.equals(periodStart.getValue())) {

                        boolean hasAlt = false;
                        // check if period has alternate link today
                        for (Triplet<Integer, DayOfWeek, String> altLink : options.altLinks) {
                            if (periodStart.getKey().equals(altLink.getV1()) && altLink.getV2().equals(day)) {
                                hasAlt = true;
                                openLink(altLink.getV3());
                                notify("Opening Period " + periodStart.getKey() + " Alt Link", 0);
                                System.out.println("Period " + periodStart.getKey() + " alt opened\n");
                            }
                        }

                        // if no alternate link use default
                        if (!hasAlt) {
                            try {
                                openLink(options.links.get(periodStart.getKey() - 1));
                                notify("Opening Period " + periodStart.getKey() + " Link", 0);
                                System.out.println("Period " + periodStart.getKey() + " opened\n");
                            } catch (IndexOutOfBoundsException e) {
                                notify("Error: Could not open scheduled period link, check options", 3);
                            }
                        }
                    }
                }

                // check if time to show alert
                for (Triplet<DayOfWeek, LocalTime, String> alert : options.alerts) {
                    if (alert.getV1().equals(day) && alert.getV2().equals(time.minusMinutes(options.earlyMin))) {
                        notify(alert.getV3(), 0);
                        System.out.println("Alert: " + alert.getV3() + "\n");
                    }
                }

                /*if (time.equals(LocalTime.of(15,0))) {count+=960;}
                Thread.sleep(125);
                count++;*/
            }

            if (first) { notify("Launched Successfully", 0); first = false;}

            Thread.sleep(60000);
        }
    }

    public static void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) { e.printStackTrace(); }
    }

    public static void notify(String text, int level) {
        Platform.runLater(() -> HelperUI.notify(text, level));
    }
}
