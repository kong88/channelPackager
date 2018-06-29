package com.umeng.editor;

import java.util.Arrays;
import java.util.List;

/**
 * Created by wisly on 2016/6/28.
 */
public class Main {
    public static void main(String[] args) {
        int length = args.length;
        String baseDirPath = length < 1 ? "run-src" : args[0];
        String apkName = length < 2 ? "vipshop_base.apk" : args[1];
        String channelFile = length < 3 ? "run-src/channels.txt" : args[2];
        String versionName = length < 4 ? "8.5.10.2" : args[3];

        List argList = Arrays.asList(args);
        boolean batchMode = argList.contains("batchMode");
        boolean restoreSo = argList.contains("restoreSo");

        PackageUtil.channelPackage(baseDirPath, apkName, channelFile, versionName, batchMode, restoreSo);
    }
}
