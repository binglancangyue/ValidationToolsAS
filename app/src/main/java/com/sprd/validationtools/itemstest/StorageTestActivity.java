package com.sprd.validationtools.itemstest;

import android.annotation.TargetApi;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;
import android.os.SystemProperties;

import com.android.internal.util.MemInfoReader;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.StoragePaTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class StorageTestActivity extends BaseActivity {
    private static final String TAG = "StorageTestActivity";
    private TextView tvEMC;
    private TextView tvDDR;
    private float unit;
    private long totalSize = 0L;
    private static final String CONFIG_RAM_SIZE = "ro.deviceinfo.ram";
    private static final String SPRD_RAM_SIZE = "ro.ramsize";
    private static final long KB_IN_BYTES = 1024;
    private static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
    private static final long[] RAM_SIZE = new long[]{
            128 * MB_IN_BYTES, 256 * MB_IN_BYTES,
            512 * MB_IN_BYTES
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_test);
        initView();
    }

    private void initView() {
        tvEMC = findViewById(R.id.tv_title_emc);
        tvDDR = findViewById(R.id.tv_title_ddr);
        tvDDR.setText(getDDR());
        tvEMC.setText(getTotalSize());
    }

    private String getDDR() {
        String configRam = getConfigRam();
        String ddr = null;
        if (configRam == null) {
            Log.d(TAG, "not config RAM, so read RAM value!" + "\nddr " + ddr);
            ddr = getRamSizeFromProperty();
        } else {
            Log.d(TAG, "set config RAM: " + configRam);
            ddr = configRam;
        }
        return ddr;
    }

    public String getConfigRam() {
        String ramConfig = SystemProperties.get(CONFIG_RAM_SIZE, "unconfig");
        if ("unconfig".equals(ramConfig)) {
            Log.d(TAG, "ramConfig no value");
            return null;
        } else {
            long configTotalRam = Long.parseLong(ramConfig);
            Log.d(TAG, "config ram to be: " + configTotalRam);
            return Formatter.formatShortFileSize(this, configTotalRam);
        }
    }

    public String getRamSizeFromProperty() {
        long realRamSize = 0;
        long size = SystemProperties.getLong(SPRD_RAM_SIZE, 0);
        if (size == 0) {
            Log.d(TAG, "Property:ro.ramsize no value, so read FW set value");
            return getRamSizeFromFW();
        } else {
            realRamSize = size / 1024;
            Log.d(TAG, "getRamSizeFromProperty, value is:" + size + " realRamSize " + realRamSize);
            return realRamSize + " GB";
        }
    }

    public String getRamSizeFromFW() {
        long formatRamSize = 0;
        MemInfoReader memReader = new MemInfoReader();
        memReader.readMemInfo();
        long readTotalRam = memReader.getTotalSize();
        Log.d(TAG, "getRamSizeFromFW, readTotalRam: " + readTotalRam);

        for (int i = 0; i < RAM_SIZE.length; i++) {
            if (readTotalRam <= RAM_SIZE[i]) {
                formatRamSize = RAM_SIZE[i];
                break;
            }
            if (i == (RAM_SIZE.length - 1)) {
                formatRamSize = roundRamSize(readTotalRam);
            }
        }
        Log.d(TAG, "formatRamSize: " + formatRamSize);

        return Formatter.formatShortFileSize(this, formatRamSize);
    }

    /**
     * Round the given size of a storage device to a nice round power-of-two
     * value, such as 1G or 3GB. This avoids showing weird values like
     * "29.5GB" in UI.
     */
    private long roundRamSize(long size) {
        long val = 1;
        long pow = 1;
        while ((val * pow) < size) {
            val += 1;
            if (val > 512) {
                val = 1;
                pow *= 1024;
            }
        }
        return val * pow;
    }

    private String getTotalSize() {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        try {
            Method getVolumes = StorageManager.class.getDeclaredMethod("getVolumes");//6.0
            List<Object> getVolumeInfo = (List<Object>) getVolumes.invoke(storageManager);
            long total = 0L, used = 0L;
            for (Object obj : getVolumeInfo) {
                Field getType = obj.getClass().getField("type");
                int type = getType.getInt(obj);
                Log.d(TAG, "type: " + type);
                if (type == 1) {//TYPE_PRIVATE

                    //获取内置内存总大小
                    unit = 1000;
                    Method getFsUuid = obj.getClass().getDeclaredMethod("getFsUuid");
                    String fsUuid = (String) getFsUuid.invoke(obj);
                    totalSize = getTotalSize(fsUuid);//8.0 以后使用

                    long systemSize = 0L;
                    Method isMountedReadable = obj.getClass().getDeclaredMethod("isMountedReadable");
                    boolean readable = (boolean) isMountedReadable.invoke(obj);
                    if (readable) {
                        Method file = obj.getClass().getDeclaredMethod("getPath");
                        File f = (File) file.invoke(obj);

                        if (totalSize == 0) {
                            totalSize = f.getTotalSpace();
                        }
                        String _msg = "剩余总内存：" + getUnit(f.getTotalSpace(), unit) + "\n可用内存：" + getUnit(f.getFreeSpace(), unit) + "\n已用内存：" + getUnit(f.getTotalSpace() - f.getFreeSpace(), unit);
                        Log.d(TAG, _msg);
                        systemSize = totalSize - f.getTotalSpace();
                        used += totalSize - f.getFreeSpace();
                        total += totalSize;
                    }
                    Log.d(TAG, "totalSize = " + getUnit(totalSize, unit) + " ,used(with system) = " + getUnit(used, unit) + " ,free = " + getUnit(totalSize - used, unit));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getUnit(totalSize, unit);
    }

    /**
     * API 26 android O
     * 获取总共容量大小，包括系统大小
     */
    private long getTotalSize(String fsUuid) {
        try {
            UUID id;
            if (fsUuid == null) {
                id = StorageManager.UUID_DEFAULT;
            } else {
                id = UUID.fromString(fsUuid);
            }
            StorageStatsManager stats = getSystemService(StorageStatsManager.class);
            return stats.getTotalBytes(id);
        } catch (NoSuchFieldError | NoClassDefFoundError | NullPointerException | IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private String[] units = {"B", "KB", "MB", "GB", "TB"};

    /**
     * 进制转换
     */
    public String getUnit(float size, float base) {
        int index = 0;
        while (size > base && index < 4) {
            size = size / base;
            index++;
        }
        return String.format(Locale.getDefault(), " %.2f %s ", size, units[index]);
    }

}
