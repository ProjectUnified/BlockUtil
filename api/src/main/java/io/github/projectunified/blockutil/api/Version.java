package io.github.projectunified.blockutil.api;

import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
    private static final int MAJOR_VERSION;
    private static final int MINOR_VERSION;

    static {
        Matcher versionMatcher = Pattern.compile("MC: \\d\\.(\\d+)(\\.(\\d+))?").matcher(Bukkit.getVersion());
        if (versionMatcher.find()) {
            MAJOR_VERSION = Integer.parseInt(versionMatcher.group(1));
            MINOR_VERSION = Optional.ofNullable(versionMatcher.group(3)).filter(s -> !s.isEmpty()).map(Integer::parseInt).orElse(0);
        } else {
            MAJOR_VERSION = -1;
            MINOR_VERSION = -1;
        }
    }

    public static boolean isAtLeast(int major, int minor) {
        return MAJOR_VERSION > major || (MAJOR_VERSION == major && MINOR_VERSION >= minor);
    }

    public static boolean isAtLeast(int major) {
        return MAJOR_VERSION >= major;
    }

    public static boolean isFlat() {
        return isAtLeast(13);
    }
}
