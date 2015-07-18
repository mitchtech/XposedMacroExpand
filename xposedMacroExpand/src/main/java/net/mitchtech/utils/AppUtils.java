package net.mitchtech.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

public class AppUtils {

    private static final String TAG = AppUtils.class.getSimpleName();

    public static String getVersion(Context context) {
        String version = "";
        try {
            PackageInfo pi = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            version = " v" + pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found", e);
        }
        return version;
    }

    public static Class<?> getLauncherClass(Context context) {
        String className = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName())
                .getComponent().getClassName();
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        return clazz;
    }

    public static void reloadLauncherActivity(Context context) {
        Class<?> clazz = getLauncherClass(context);
        Intent intent = new Intent(context, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static String getCurrentActiveApp(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        String packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        return packageName;
    }

    public static List<ActivityManager.RunningTaskInfo> getCurrentRunningApps(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (int i = 0; i < tasks.size(); i++) {
            Log.i("Running task", "Running task: " + tasks.get(i).baseActivity.toShortString() + "\t\t ID: " + tasks.get(i).id);
        }
        return tasks;
    }

    public static String getCurrentApps(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        String apps = "";
        for (int i = 0; i < tasks.size(); i++) {
//            Log.i("Running task", "Running task: " + tasks.get(i).baseActivity.toShortString() + "\t\t ID: " + tasks.get(i).id);
//            apps += tasks.get(i).baseActivity.toShortString() + "\t\t ID: " + tasks.get(i).id;
//            apps += "[" + tasks.get(i).id + "]" + tasks.get(i).baseActivity.toShortString() + ",";
            apps += tasks.get(i).baseActivity.toShortString() + ",";

        }
        return apps;
    }

    public static List<PackageInfo> getInstalledApps(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (PackageInfo app : packages) {
            Log.i(TAG, "" + app.packageName + ":" + app.applicationInfo.name);
//           for (PermissionInfo permission : app.permissions) {
//               Log.i(TAG, "permission:" + permission.name);
//           }
        }

        return packages;
    }

}
