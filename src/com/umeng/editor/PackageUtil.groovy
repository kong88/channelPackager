package com.umeng.editor

import com.umeng.editor.decode.AXMLDoc
import com.umeng.editor.utils.SystemUtils

import java.security.MessageDigest

/**
 * Created by wisly on 2016/6/28.
 * --
 */
public class PackageUtil {

    private final static String CLEAR_FILES = "temp.apk AndroidManifest.xml META-INF lib"
    private final static String TEMP_APK = "/temp.apk";
    private final static String MANIFEST_FILE = "AndroidManifest.xml";
    private final static String META_INF_DIR = "META-INF";

    static class FlavorModule {
        String flavorName
        String channelName
        String channelValue
        String channelFileName
    }

    private static void printResult(String result, boolean failed) {
        if (failed) {
            println(">>>>>>>>>>>>>>> " + result + " OK");
        } else {
            println("!!!!!!!!!!!!!!! " + result + " Failed");
        }
    }

    // 执行命令行语句
    private static boolean exec(cmd) {
        print("" + cmd + "\n")
        def proc = cmd.execute();
        proc.waitFor(); //用以等待外部进程调用结束
        println proc.getText();
        boolean result = proc.exitValue() == 0;
        proc.destroy();
        return result;
    }

    // command: cp $srcPath $destPath
    private static boolean copyFile(String srcPath, String destPath) {
        def cmd = "cp " + srcPath + " " + destPath;
        boolean result = exec(cmd);
        printResult("copyFile", result);
        return result;
    }

    // 解压Manifest文件
    private static boolean unzipManifest(String apkPath) {
        def cmd = "unzip " + apkPath + " " + MANIFEST_FILE;
        boolean result = exec(cmd);
        printResult("unzipManifest", result);
        return result;
    }

    //从压缩包删除文件
    private static boolean deleFromApk(String apkPath, String fileName) {
        def cmd = "zip -d " + apkPath + " " + fileName;
        return exec(cmd);
    }

    // 往apk更新文件
    private static boolean updateApk(String apkPath, String fileName, boolean storeMode) {
        deleFromApk(apkPath, fileName)
        if (fileName.contains("/*")) {
            fileName = fileName.substring(0, fileName.indexOf("/*") + 1);
        }
        def cmd = "zip -D -r " + (storeMode ? "-0 " : "") + apkPath + " " + fileName;
        boolean result = exec(cmd);
        printResult("updateApk " + fileName, result);
        return result;
    }

    private static String encoderByMd5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            md5.update(str.getBytes());
            byte[] bts = md5.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bts.length; i++) {
                String tmp = (Integer.toHexString(bts[i] & 0xFF));
                if (tmp.length() == 1) {
                    sb.append('0');
                }
                sb.append(tmp);
            }
            return sb.toString();
        } catch (Exception ignored) {
            return str;
        }
    }

    static boolean modifyManifestFile(String inputPath, String channelName, String channelValue, String outputPath) {
        try {
            AXMLDoc doc = new AXMLDoc();
            doc.parse(new FileInputStream(inputPath));
            // doc.print();

            ChannelEditor2 editor = new ChannelEditor2(doc);
            editor.setChannel(channelName);
            editor.setCPS_ID(channelValue);
            editor.commit();

            doc.build(new FileOutputStream(outputPath));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        printResult("modifyManifestFile", true);
        return true;
    }

    private
    static boolean writeChannelFile(String path, String channelName, String channelValue, String channelFileName) {
        File file = new File(path);
        if (file.exists()) {
            file.deleteDir();
        }
        file.mkdirs();

        File channelFile = new File(path + "/" + channelFileName);
        channelFile.createNewFile();
        channelFile.write(channelName + "," + channelValue, "utf-8");
        printResult("writeChannelFile", true);
        return true;
    }

    @SuppressWarnings("GroovyMissingReturnStatement")
    private static Map<String, FlavorModule> getChannels(String channelFilePath, boolean isBatch) {
        Map<String, FlavorModule> channelsAll = new HashMap<String, FlavorModule>();
        // 渠道号配置文件路径
        int i = 0;
        File file = new File(channelFilePath);
        file.eachLine { line ->
            if (!line.startsWith("//")) { //剔除注释行
                String[] channel = line.split(",")
                if (!isBatch && (channel.length == 3 || channel.length == 4)) {
                    //非批量
                    i++
                    println("--------------------" + i + " " + line)
                    FlavorModule module = new FlavorModule()
                    module.flavorName = channel[0]
                    module.channelName = channel[1]
                    module.channelValue = channel[2]
                    if (channel.length == 4) {
                        module.channelFileName = channel[3]
                    } else {
                        module.channelFileName = 'vipchannel.txt';
                    }
                    channelsAll.put("${module.flavorName}", module)
                } else if (isBatch && (channel.length == 2 || channel.length == 3)) {
                    //批量
                    i++
                    println("--------------------" + i + " " + line)
                    FlavorModule module = new FlavorModule()
                    module.flavorName = 'favername' + i
                    module.channelName = channel[0]
                    module.channelValue = channel[1]
                    if (channel.length == 3) {
                        module.channelFileName = channel[2]
                    } else {
                        module.channelFileName = 'vipchannel.txt';
                    }
                    channelsAll.put("${module.flavorName}", module)
                }
            }
        }
        return channelsAll
    }

    private static String getGitName(String apkName) {
        String gitName = "";
        try {
            int index = apkName.indexOf("git");
            if (index != -1) {
                gitName = apkName.substring(index, index + 11);
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
        return "_" + gitName;
    }

    // 以store方式重新打包
    private static boolean storeSoFiles(String apkPath) {
        def extractString = " lib/*.so"
        if (SystemUtils.isWindows()) {
            extractString = " lib/*/*.so"
        }
        def cmd = "unzip " + apkPath + extractString;
        exec(cmd);
        boolean result = updateApk(apkPath, extractString, true)
        printResult("storeSoFiles ", result);
        return result;
    }

    // 对so做4K对齐
    private static boolean zipAlign(String apkPath) {
        String tempFile = apkPath + ".2";
        String osName = SystemUtils.getOSName();
        def cmd = "./zipalign -f -p 4 " + apkPath + " " + tempFile;// mac or linux
        if (osName != null && osName.toLowerCase().contains("window")) {
            cmd = "./zipalign.exe -f -p 4 " + apkPath + " " + tempFile;// windows
        }

        boolean result = exec(cmd);
        printResult("zipAlign ", result);
        if (result) {
            File file = new File(tempFile);
            if (file.exists()) {
                file.renameTo(apkPath);
            }
        }
        return result;
    }

    private static boolean clearFiles(String file) {
        exec("rm -r " + file);
    }

    /**
     * Main
     */
    public static void channelPackage(String baseDir, String apkName, String channelFile, String versionName,
                                      boolean batchMode, boolean resotreSo) {

        // 相对路径换成绝对路径
        if (baseDir.contains(":") || baseDir.startsWith("/") || baseDir.startsWith("./")) {
            baseDir = System.getProperty("user.dir") + "/" + baseDir;
        }
        String apkPath = baseDir + TEMP_APK;

        boolean result = copyFile(baseDir + "/" + apkName, apkPath);
        if (result) {
            clearFiles(MANIFEST_FILE)
            result = unzipManifest(apkPath);
        }

        if (result) {
            List<FlavorModule> flavorList = new ArrayList<FlavorModule>();
            File file = new File(baseDir + "/apk");
            if (file.exists()) {
                file.deleteDir();
            }
            file.mkdirs();

            Map<String, FlavorModule> channelsAll = getChannels(channelFile, batchMode);
            channelsAll.each {
                String flavorName = it.getKey();
                String channelName = it.value.channelName;
                String channelValue = it.value.channelValue;
                String flavor = channelName;
                String channelFileName = it.value.channelFileName;
                try {
                    flavor = "vipshop_" + flavorName + "_" + versionName + getGitName(apkName) + "_" + new Date().format("yyyy-MM-dd", TimeZone.getTimeZone("UTC"));
                    if (batchMode) {
                        String apkPartName;
                        try {
                            apkPartName = apkName.substring(0, apkName.lastIndexOf("-"))
                            apkPartName = apkPartName.substring(apkPartName.indexOf("8."))
                            apkPartName = apkPartName.substring(0, apkPartName.indexOf("_"));
                        } catch (Exception ignored) {
                            apkPartName = versionName;
                        }
                        flavor = "shop_android_" + encoderByMd5(channelValue) + "_" + apkPartName;
                        FlavorModule temp = new FlavorModule();
                        temp.channelName = channelName;
                        temp.channelValue = channelValue;
                        temp.flavorName = flavor;
                        flavorList.add(temp)
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                result = modifyManifestFile(baseDir + "/" + MANIFEST_FILE, channelName, channelValue, baseDir + "/" + MANIFEST_FILE);
                if (result) {
                    result = writeChannelFile(baseDir + "/" + META_INF_DIR, channelName, channelValue, channelFileName);
                }
                if (result) {
                    deleFromApk(apkPath, META_INF_DIR + "/*.MF");
                    deleFromApk(apkPath, META_INF_DIR + "/*.SF");
                    deleFromApk(apkPath, META_INF_DIR + "/*.RSA");
                    result = updateApk(apkPath, META_INF_DIR + "/" + channelFileName, false);
                }
                if (result) {
                    result = updateApk(apkPath, MANIFEST_FILE, false);
                }
                if (resotreSo && result) {
                    result = storeSoFiles(apkPath)
                }
                String outputFile = baseDir + "/apk/" + flavor + ".apk";
                if (result) {
                    result = Signe.signe(baseDir + "/android.keystore", outputFile, apkPath);
                    printResult("signature", result)
                }
                if (result) {
                    result = zipAlign(outputFile);
                }
            }

            if (batchMode) {
                File fMarkApkChannelValue = new File(baseDir + "/apk/apk_updateChannels.txt");
                if (!fMarkApkChannelValue.exists()) {
                    fMarkApkChannelValue.createNewFile();
                } else {
                    fMarkApkChannelValue.deleteDir();
                    fMarkApkChannelValue.createNewFile();
                }

                def printWriter = fMarkApkChannelValue.newPrintWriter();
                for (int i = 0; i < flavorList.size(); i++) {
                    printWriter.println(flavorList.get(i).channelName + "," + flavorList.get(i).channelValue + "," + flavorList.get(i).flavorName + ".apk");
                }
                printWriter.flush();
                printWriter.close();
            }
        }

        clearFiles(CLEAR_FILES)
    }
}
