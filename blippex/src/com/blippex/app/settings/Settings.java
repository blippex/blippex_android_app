package com.blippex.app.settings;

import com.blippex.app.Blippex;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
	private static final String PREF_DWELL = "PREF_DWELL";
	private static final String PREF_SEEN = "PREF_SEEN";
	
	private static final Integer PREF_DWELL_DEF = 10;
	private static final Integer PREF_DWELL_SEEN = 30;

	public static Integer dwell() {
		return getInt(PREF_DWELL, PREF_DWELL_DEF);
	}

	public static Integer seen() {
		return getInt(PREF_SEEN, PREF_DWELL_SEEN);
	}

	public static void dwell(Integer value) {
		setInt(PREF_DWELL, value);
	}

	public static void seen(Integer value) {
		setInt(PREF_SEEN, value);
	}

	public static void searchCount(Integer counter) {

	}

	private static Integer getInt(String key, Integer defaultVal) {
		return PreferenceManager.getDefaultSharedPreferences(
				Blippex.getAppContext()).getInt(key, defaultVal);
	}

	private static void setInt(String key, Integer value) {
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(Blippex.getAppContext()).edit();
		editor.putInt(key, value);
		editor.commit();
	}

}
