package io.github.projectunified.blockutil.spigot.common;

import org.bukkit.Bukkit;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
    private static final int MAJOR_VERSION;
    private static final int MINOR_VERSION;
    private static final int PATCH_VERSION;

    static {
        Matcher versionMatcher = Pattern.compile("MC: (\\d+)\\.(\\d+)(\\.(\\d+))?").matcher(Bukkit.getVersion());
        if (versionMatcher.find()) {
            int majorVersion = Integer.parseInt(versionMatcher.group(1));
            int minorVersion = Integer.parseInt(versionMatcher.group(2));
            int patchVersion = Optional.ofNullable(versionMatcher.group(4)).filter(s -> !s.isEmpty()).map(Integer::parseInt).orElse(0);
            if (majorVersion == 1) {
                MAJOR_VERSION = minorVersion;
                MINOR_VERSION = patchVersion;
                PATCH_VERSION = 0;
            } else {
                MAJOR_VERSION = majorVersion;
                MINOR_VERSION = minorVersion;
                PATCH_VERSION = patchVersion;
            }
        } else {
            MAJOR_VERSION = -1;
            MINOR_VERSION = -1;
            PATCH_VERSION = -1;
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
