package com.smartbear;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class Version {
    private int majorVersion;
    private int minorVersion;
    private String patchVersion;

    public Version(int majorVersion, int minorVersion, String patchVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.patchVersion = patchVersion;
    }

    @NotNull
    public static Version getFromString(String versionString) {
        Version version = getDefaultVersion();
        if (StringUtils.isNotBlank(versionString)) {
            String[] versionParts = versionString.split("\\.");
            version.majorVersion = Integer.parseInt(versionParts[0]);
            version.minorVersion = Integer.parseInt(versionParts[1]);
            if (versionParts.length > 2) {
                version.patchVersion = versionParts[2];
            }
        }
        return version;
    }

    @NotNull
    public static Version getDefaultVersion() {
        return new Version(0, 0, "0");
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public String getPatchVersion() {
        return patchVersion;
    }
}
