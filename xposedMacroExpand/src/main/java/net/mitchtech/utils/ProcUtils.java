package net.mitchtech.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ProcUtils {

    private static final String TAG = ProcUtils.class.getSimpleName();

    public static String getVersion() {
        return readFile("proc/version");
    }

    public static String getUptime() {
        return readFile("proc/uptime");
    }

    public static String getMemInfo() {
        return readFile("proc/meminfo");
    }

    public static String getCpuInfo() {
        return readFile("proc/cpuinfo");
    }

    public static String getCmdLine() {
        return readFile("proc/cmdline");
    }

    public static long getCpuTime() {
        String cpustat = readFile("proc/stat");
        if (cpustat == null) {
            return 0;
        }
        String[] segs = cpustat.split("[ ]+");
        return Long.parseLong(segs[1]) + Long.parseLong(segs[2])
                + Long.parseLong(segs[3]) + Long.parseLong(segs[4]);
    }

    private static String readFile(String filename) {
        FileReader fstream;
        try {
            fstream = new FileReader(filename);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Read error on " + filename);
            return "?";
        }

        BufferedReader in = new BufferedReader(fstream, 500);
        try {
            return in.readLine();
        } catch (IOException e) {
            Log.i(TAG, "Read error on " + filename);
            return "?";
        }
    }

}
