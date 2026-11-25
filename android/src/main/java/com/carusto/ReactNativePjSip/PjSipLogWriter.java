package com.carusto.ReactNativePjSip;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.pjsip.pjsua2.LogEntry;
import org.pjsip.pjsua2.LogWriter;
import org.pjsip.pjsua2.pjsua2JNI;

public class PjSipLogWriter extends LogWriter {

    private static String TAG = "PjSipLogWriter";

    // pjsip log level (1=error ... 4=info incl. SIP messages) up to which entries are sent to JS,
    // 0 = nothing. Set from JS via PjSipModule.setLogLevel while the app logs.
    private static volatile int forwardLevel = 0;

    // native level the stack logs with, entries above forwardLevel are only a cheap int compare
    public static final int NATIVE_LEVEL = 4;

    private Context context;

    public PjSipLogWriter(Context context) {
        this.context = context;
    }

    public static void setForwardLevel(int level) {
        forwardLevel = level;
    }

    public void write(LogEntry entry) {
        int level = entry.getLevel();
        if (level <= 1) {
            Log.d(TAG, entry.getMsg());
        }
        if (level > forwardLevel || context == null) {
            return;
        }
        // called on pjsip threads: never call back into pjsip here
        try {
            Intent intent = new Intent();
            intent.setAction(PjActions.EVENT_LOG);
            intent.setPackage(context.getPackageName());
            intent.putExtra("level", level);
            intent.putExtra("msg", entry.getMsg());
            context.sendBroadcast(intent);
        } catch (Exception e) {
            // ignore
        }
    }

}
