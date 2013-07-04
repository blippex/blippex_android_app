package com.blippex.app.misc;

import android.util.Log;

public class Logger {
	private static Logger logger = new Logger();

	private boolean isDebug = true;

	public static Logger getDefault() {
		return logger;
	}

	public boolean isDebug() {
		return isDebug;
	}

	public void error(String message, Throwable e) {
		Log.e("Archify", message, e);
	}

	public void debug(String message) {
		if (isDebug) {
			Log.d("Archify", message);
		}
	}

	public void info(String message) {
		Log.i("Archify", message);
	}

	public void trace(String message) {
		if (isDebug) {
			Log.d("Archify", message);
		}
	}

}
