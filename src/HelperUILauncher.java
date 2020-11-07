import javafx.application.Application;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.getProperty;

public class HelperUILauncher {

    public static void main(String[] args) {
        new Thread(() -> Application.launch(HelperUI.class, args)).start();
    }

    public static void launchProcess(String ... args) {
        System.out.println("gui launch 🚀");
        ArrayList<String> commands = new ArrayList<>(Arrays.asList(
                Paths.get(getProperty("java.home"), "bin", "java").toString(),
                "-Xms10M", "-Xmx100M", "-XX:+UseG1GC",
                "-classpath", getProperty("java.class.path"), HelperUILauncher.class.getName()));
        commands.addAll(Arrays.asList(args));
        try {
            new ProcessBuilder(commands).inheritIO().start();
        } catch (IOException ex) { throw new UncheckedIOException(ex); }
    }
}