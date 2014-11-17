package com.lica.wifistorage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class Logger {

	private static final int VERBOSE = 5;
	private static final int DEBUG = 4;
	private static final int INFO = 3;
	private static final int WARN = 2;
	private static final int ERROR = 1;

	private static final int LOG_LEVEL = INFO;

    private static Handler sHandler;

    public static void setLogHandler(Handler handler) {
        sHandler = handler;
    }

    public static void v(String tag, String msg) {
		if (LOG_LEVEL >= VERBOSE && !TextUtils.isEmpty(msg)) {
			Log.v(tag, msg);
            if (sHandler != null) {
                Bundle bundle = new Bundle();
                bundle.putString("text", msg);
                Message message = new Message();
                message.what = Log.VERBOSE;
                message.setData(bundle);
                sHandler.sendMessage(message);
            }
		}
	}

	public static void d(String tag, String msg) {
		if (LOG_LEVEL >= DEBUG && !TextUtils.isEmpty(msg)) {
			Log.d(tag, msg);
            if (sHandler != null) {
                Bundle bundle = new Bundle();
                bundle.putString("text", msg);
                Message message = new Message();
                message.what = Log.DEBUG;
                message.setData(bundle);
                sHandler.sendMessage(message);
            }
		}
	}

	public static void i(String tag, String msg) {
		if (LOG_LEVEL >= INFO && !TextUtils.isEmpty(msg)) {
			Log.i(tag, msg);
            if (sHandler != null) {
                Bundle bundle = new Bundle();
                bundle.putString("text", msg);
                Message message = new Message();
                message.what = Log.INFO;
                message.setData(bundle);
                sHandler.sendMessage(message);
            }
		}
	}

	public static void w(String tag, String msg) {
		if (LOG_LEVEL >= WARN && !TextUtils.isEmpty(msg)) {
			Log.w(tag, msg);
            if (sHandler != null) {
                Bundle bundle = new Bundle();
                bundle.putString("text", msg);
                Message message = new Message();
                message.what = Log.WARN;
                message.setData(bundle);
                sHandler.sendMessage(message);
            }
		}
	}

	public static void e(String tag, String msg) {
		if (LOG_LEVEL >= ERROR && !TextUtils.isEmpty(msg)) {
			Log.e(tag, msg);
            if (sHandler != null) {
                Bundle bundle = new Bundle();
                bundle.putString("text", msg);
                Message message = new Message();
                message.what = Log.ERROR;
                message.setData(bundle);
                sHandler.sendMessage(message);
            }
		}
	}
}
