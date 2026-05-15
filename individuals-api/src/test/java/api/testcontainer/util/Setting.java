package api.testcontainer.util;

import org.testcontainers.containers.Network;

import java.io.File;
import java.nio.file.Path;

public class Setting {

    public static final Network GLOBAL_NETWORK = Network.newNetwork();
    public static final String CLASS_PATH = Path.of("").toAbsolutePath().toString();
    public static final File PROJECT_ROOT = new File(CLASS_PATH);


    public static Path realmInitPath() {
        return Path.of(toAbsolute()).resolve("src/test/resources/realm-config.json");
    }

    private static String toAbsolute() {
        return Setting.PROJECT_ROOT.toPath().toAbsolutePath().toString();
    }
}
