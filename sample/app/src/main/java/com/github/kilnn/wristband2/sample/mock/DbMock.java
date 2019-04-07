package com.github.kilnn.wristband2.sample.mock;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Mock database cache
 */
public class DbMock {

    private static final String TAG = "DbMock";

    /**
     * Conversion device address as database key
     * 00:00:00:00:00:12 -> device_000000000012
     */
    private static String getKey(BluetoothDevice device) {
        String address = device.getAddress().replaceAll(":", "");
        String key = "device_" + address;
        Log.d(TAG, "getKey:" + key);
        return key;
    }

    /**
     * Query whether the device is bound to this user
     *
     * @return True for bind ,false for not.
     */
    public static boolean isUserBind(Context context, BluetoothDevice device, User user) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int bindUserId = sharedPreferences.getInt(getKey(device), 0);
        return bindUserId == user.getId();
    }

    /**
     * Set the user bind with the device
     */
    public static void setUserBind(Context context, BluetoothDevice device, User user) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt(getKey(device), user.getId()).apply();
    }

    /**
     * Clear the bind info
     */
    public static void clearUserBind(Context context, BluetoothDevice device) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt(getKey(device), 0).apply();
    }
}
