package net.mitchtech.utils;

import android.os.Environment;
import android.os.StatFs;

public class StorageUtils {

    private static final String TAG = StorageUtils.class.getSimpleName();

    public static long getInternalAvailableSpace() {
        long availableSpaceKb = -1L;
        long availableSpaceMb = -1L;
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            stat.restat(Environment.getDataDirectory().getPath());
            availableSpaceKb = ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize()) / (long) 1024;
            availableSpaceMb = availableSpaceKb / (long) 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availableSpaceMb;
    }

    public static long getExternalAvailableSpace() {
        long availableSpaceKb = -1L;
        long availableSpaceMb = -1L;
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            stat.restat(Environment.getExternalStorageDirectory().getPath());
            availableSpaceKb = ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize()) / (long) 1024;
            availableSpaceMb = availableSpaceKb / (long) 1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return availableSpaceMb;
    }
}
