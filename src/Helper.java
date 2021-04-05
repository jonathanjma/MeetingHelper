import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@SuppressWarnings("InfiniteLoopStatement")
public class Helper {

    private static TrayIcon trayIcon;
    public final static String path = System.getProperty("user.home")+"/helper_options.xml";
    public final static double version = 3.1;

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Starting MeetingHelper v" + version);
        HelperUILauncher.launchProcess("options");

        // set up tray icon
        try {
            SystemTray tray = SystemTray.getSystemTray();
            BufferedImage img = ImageIO.read(Helper.class.getResource("res/tray.png"));
            trayIcon = new TrayIcon(
                    img.getScaledInstance(tray.getTrayIconSize().width, -1, Image.SCALE_SMOOTH),
                    "Meeting Helper :-)");

            PopupMenu popup = new PopupMenu();
            MenuItem todayItem = new MenuItem("Today View");
            MenuItem optionsItem = new MenuItem("Options");
            MenuItem aboutItem = new MenuItem("About");
            MenuItem exitItem = new MenuItem("Exit");
            popup.add(todayItem); popup.add(optionsItem); popup.add(aboutItem);
            popup.addSeparator(); popup.add(exitItem);

            todayItem.addActionListener(e -> HelperUILauncher.launchProcess("today"));
            optionsItem.addActionListener(e -> HelperUILauncher.launchProcess("options"));
            aboutItem.addActionListener(e -> HelperUILauncher.launchProcess("about"));
            exitItem.addActionListener(e -> {
                notify("Exiting", 1);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) { ex.printStackTrace(); }
                tray.remove(trayIcon); System.exit(0);
            });
            trayIcon.setPopupMenu(popup);

            // alternate ways of opening menu/ui if right click does not work
            Frame frame = new Frame("");
            frame.setUndecorated(true);
            frame.setType(Window.Type.UTILITY);
            frame.setResizable(false);
            frame.setVisible(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        frame.add(popup);
                        popup.show(frame, e.getXOnScreen(), e.getYOnScreen());
                    }
                    if (e.getClickCount() >= 2) {
                        HelperUILauncher.launchProcess("today");
                    }
                }
            });

            tray.add(trayIcon);
        } catch (IOException | AWTException ex) {
            ex.printStackTrace();
            notify("Tray Error, Exiting", 3); Thread.sleep(3000);
            System.exit(-1);
        }

        OptionsUtil options = new OptionsUtil();

        // create options file if it does not exist
        try {
            if (!new File(Helper.path).exists()) { options.createOptions(); }
        } catch (IOException ex) { ex.printStackTrace(); }

        boolean first = true;
        while (true) {

            // load options as options could have been changed
            try {
                options.updateOptionsVars();
            } catch (Exception ex) { ex.printStackTrace(); }

            LocalDateTime now = LocalDateTime.now();
            DayOfWeek day = now.getDayOfWeek();

            // if not weekends
            if (day.getValue() >= 1 && day.getValue() <= 5) {

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
                                notify("Opening Period " + periodStart.getKey() + " Alt Link", 1);
                                System.out.println("Period " + periodStart.getKey() + " alt opened\n");
                            }
                        }

                        // if no alternate link use default
                        if (!hasAlt) {
                            try {
                                openLink(options.links.get(periodStart.getKey() - 1));
                                notify("Opening Period " + periodStart.getKey() + " Link", 1);
                                System.out.println("Period " + periodStart.getKey() + " opened\n");
                            } catch (IndexOutOfBoundsException e) {
                                notify("Error: Could not open period link, check options", 3);
                            }
                        }
                    }
                }

                // check if time to show alert
                for (Triplet<DayOfWeek, LocalTime, String> alert : options.alerts) {
                    if (alert.getV1().equals(day) && alert.getV2().equals(time.minusMinutes(options.earlyMin))) {
                        notify(alert.getV3(), 1);
                        System.out.println("Alert: " + alert.getV3() + "\n");
                    }
                }
            }

            if (first) {
                notify("Launched Successfully", 1); first = false;
            }

            // line up sleep with beginning of minute
            Thread.sleep(60000 - now.getSecond()*1000);
        }
    }

    public static void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) { e.printStackTrace(); }
    }

    public static void notify(String text, int level) {
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
