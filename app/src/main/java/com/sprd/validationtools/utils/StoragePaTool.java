package com.sprd.validationtools.utils;

import android.content.Context;
import android.os.storage.StorageManager;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StoragePaTool {
    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     *
     * @param mContext     上下文
     * @param isRemoveAble 是否可移除，false返回内部存储，true返回外置sd卡
     * @return
     */
    public static String getStoragePath(boolean isRemoveAble, Context mContext) {
        StorageManager mStorageManager =
                (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (isRemoveAble == removable) {
                    return path;

                }
            }
        } catch (ClassNotFoundException
                | InvocationTargetException
                | NoSuchMethodException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}