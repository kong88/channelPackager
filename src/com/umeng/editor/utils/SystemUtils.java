package com.umeng.editor.utils;

public class SystemUtils {

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOSArch() {
        return System.getProperty("os.arch");
    }

    public static boolean isWindows() {
        String osName = SystemUtils.getOSName();

        if (osName != null && osName.toLowerCase().contains("window")) {
            return true;
        }

        return false;
    }
}
