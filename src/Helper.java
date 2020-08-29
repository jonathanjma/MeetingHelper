import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;

public class Helper {

    private static HashMap<Integer, String> links;
    private static TrayIcon trayIcon;

    public static void main(String[] args) {

        links = new HashMap<>();

        // set up tray icon/notification stuff
        try {
            SystemTray tray = SystemTray.getSystemTray();
            BufferedImage img = ImageIO.read(Helper.class.getResource("school.png"));
            trayIcon = new TrayIcon(img, "Meeting Helper :-)");
            trayIcon.setImageAutoSize(true);

            PopupMenu popup = new PopupMenu();
            MenuItem exitItem = new MenuItem("Exit");
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            exitItem.addActionListener(e -> {
                sendNotification("Exiting");
                tray.remove(trayIcon);
                System.exit(0);
            });

            tray.add(trayIcon);
        } catch (IOException | AWTException e) {
            e.printStackTrace();
            trayIcon.displayMessage("Meeting Helper :-)", "Failed To Launch, Exiting",
                    TrayIcon.MessageType.ERROR);
            System.exit(-1);
        }

        // read options
        try (BufferedReader br = new BufferedReader(
                new FileReader(System.getProperty("user.home")+"/helper_options.txt"))) {
            String line;
            int count = 1;
            while ((line = br.readLine()) != null) {
                links.put(count, line);
                count++;
            }
            // for (int i = 1; i <= 7; i++) { System.out.println(links.get(i)); }
        } catch (IOException e) {
            e.printStackTrace();
            trayIcon.displayMessage("Meeting Helper :-)",
                    "Error: Could Not Load Options, Exiting", TrayIcon.MessageType.ERROR);
            System.exit(-1);
        }

       sendNotification("Launched Successfully");

        while (true) {
            LocalDateTime now = LocalDateTime.now();

            DayOfWeek day = now.getDayOfWeek();
            LocalTime time = LocalTime.of(now.getHour(), now.getMinute());
            if (day.getValue() >= 1 && day.getValue() <= 5) {

                int startPeriod = (day == DayOfWeek.WEDNESDAY || day == DayOfWeek.FRIDAY) ? 2 : 1;

                if (time.equals(LocalTime.of(7, 57))) {
                    openLink(startPeriod);
                } else if (day == DayOfWeek.MONDAY && time.equals(LocalTime.of(8, 47))) {
                    openLink(2);
                }
                else if (time.equals(LocalTime.of(9, 35))) {
                    sendNotification("Office Hours\n");
                }
                else if (time.equals(LocalTime.of(10, 42))) {
                    openLink(startPeriod+2);
                } else if (day == DayOfWeek.MONDAY && time.equals(LocalTime.of(11, 32))) {
                    openLink(4);
                }
                else if (time.equals(LocalTime.of(12, 20))) {
                    sendNotification("Lunch\n");
                }
                else if (time.equals(LocalTime.of(12, 52))) {
                    openLink(startPeriod+4);
                } else if (day == DayOfWeek.MONDAY && time.equals(LocalTime.of(13, 42))) {
                    openLink(6);
                }

                if (day == DayOfWeek.TUESDAY || day == DayOfWeek.THURSDAY) {
                    if (time.equals(LocalTime.of(14, 57))) {
                        openLink(7);
                    }
                    else if (time.equals(LocalTime.of(17, 0))) {
                        sendNotification("School Over\n");
                    }
                } else {
                    if (time.equals(LocalTime.of(14, 30))) {
                        sendNotification("School Over\n");
                    }
                }
            }

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public static void openLink(int pd) {
        try {
            Desktop.getDesktop().browse(new URI(links.get(pd)));
            sendNotification("Opening Period " + pd + " Link");
            System.out.println("Period " + pd + " opened\n");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void sendNotification(String text) {
        trayIcon.displayMessage("Meeting Helper :-)", text, TrayIcon.MessageType.INFO);
    }
}
