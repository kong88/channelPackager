package com.umeng.editor;

/**
 * Created by wisly on 2016/5/6.
 */
public class Signe {
    public static boolean signe(String keystorePath,String outputApk,String inputApk) {
        try {
           // String cmd = "jarsigner -verbose -keystore "+keystorePath+" -storepass android -signedjar "+outputApk+" -digestalg SHA1 -sigalg MD5withRSA "+inputApk+" android.keystore" ;
            String cmd="jarsigner -verbose -keystore "+keystorePath+" -storepass android -signedjar "+outputApk+" -digestalg SHA1 -sigalg MD5withRSA "+inputApk+" android.keystore";
            System.out.println(cmd);
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(cmd);
            //两个线程输出log
            new Thread(new StreamDrainer(process.getInputStream())).start();
            new Thread(new StreamDrainer(process.getErrorStream())).start();
            int exitValue = process.waitFor();
            System.out.println("返回值：" + exitValue);
            process.destroy();
            if(exitValue==0){
                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args){
        String baseDirPath="F:\\myProject\\channelpackage\\mylib\\test";
        Signe.signe(baseDirPath+"\\android.keystore",baseDirPath+"\\redex_signed.apk",baseDirPath+"\\"+"vipshopredex.apk");
    }

}
