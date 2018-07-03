package com.umeng.editor;

import com.umeng.editor.utils.SystemUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by wisly on 2016/6/28.
 */
public class Main {
    public static void main(String[] args) {
        int length = args.length;
        String baseDirPath = length < 1 ? "./" : args[0];
        String apkName = length < 2 ? "vipshop_base.apk" : args[1];
        String channelFile = length < 3 ? "channels.txt" : args[2];
        String versionName = length < 4 ? "8.5.29.10.1" : args[3];

        List argList = Arrays.asList(args);
        boolean batchMode = argList.contains("batchMode");
        boolean restoreSo = argList.contains("restoreSo");

        System.out.println(String.format("%s, %s, %s, %s, %b, %b", baseDirPath, apkName, channelFile, versionName, batchMode, restoreSo));

        PackageUtil.channelPackage(baseDirPath, apkName, channelFile, versionName, batchMode, restoreSo);
    }
}
