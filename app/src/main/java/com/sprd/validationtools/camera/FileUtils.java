package com.sprd.validationtools.camera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class FileUtils {

    private static final String TAG = "FileUtils";
    public static boolean copyFile(String srcPath, String destDir) {
        boolean flag = false;

        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            Log.d(TAG, "src file not exists ");
            return false;
        }
        String fileName = srcPath
                .substring(srcPath.lastIndexOf(File.separator));
        String destPath = destDir + fileName;
        if (destPath.equals(srcPath)) {
            Log.d(TAG, "src file and dest Dir in the same Dir");
            return false;
        }
        File destFile = new File(destPath);
        File destFileDir = new File(destDir);
        destFileDir.mkdirs();
        try {
            FileInputStream fis = new FileInputStream(srcPath);
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int c;
            while ((c = fis.read(buf)) != -1) {
                fos.write(buf, 0, c);
            }
            fis.close();
            fos.close();

            flag = true;
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
            return false;
        }

        if (flag) {
            Log.d(TAG,"copy success");
        }

        return flag;
    }

    public static boolean deleteFile(File file) {
        return file.delete();
    }
}
