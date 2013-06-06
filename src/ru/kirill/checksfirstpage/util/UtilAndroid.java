package ru.kirill.checksfirstpage.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by oleg on 01.06.13.
 */
public class UtilAndroid {
    public static String getIMEI(Context ctx) {
        // ToDo реализовать метод
        TelephonyManager tm=(TelephonyManager )ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if(tm==null) {
            return "IMEI-UNKNOWN"; //device doesn't support telephony service
        }
        return tm.getDeviceId(); //returns null for emulator!
    }
}
