package com.umeng.editor.utils;

public class SystemUtils {

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOSArch() {
        return System.getProperty("os.arch");
    }
}
