package com.taobao.android.compat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.NonNullByDefault;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Build.VERSION_CODES;

import com.taobao.android.compat.SharedPreferencesCompat.EditorCompat;

/**
 * Provide {@link Editor#apply()} for API level ~8 and
 * {@link #getStringSet(String, Set)} / {@link Editor#putStringSet(String, Set)} for API level ~ 10.
 *
 * @author Oasis
 */
@NonNullByDefault
public interface SharedPreferencesCompat {

	static final char STRING_SET_COMPAT_SEPARATOR = '\t';

    /**
     * Retrieve all values from the preferences.
     *
     * <p>Note that you <em>must not</em> modify the collection returned
     * by this method, or alter any of its contents.  The consistency of your
     * stored data is not guaranteed if you do.
     *
     * @return Returns a map containing a list of pairs key/value representing
     * the preferences.
     *
     * @throws NullPointerException
     */
    Map<String, ?> getAll();

    /**
     * Retrieve a String value from the preferences.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     *
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a String.
     *
     * @throws ClassCastException
     */
    String getString(String key, String defValue);

    /**
     * Retrieve a set of String values from the preferences.
     *
     * <p>Note that you <em>must not</em> modify the set instance returned
     * by this call.  The consistency of the stored data is not guaranteed
     * if you do, nor is your ability to modify the instance at all.
     *
     * @param key The name of the preference to retrieve.
     * @param defValues Values to return if this preference does not exist.
     *
     * @return Returns the preference values if they exist, or defValues.
     * Throws ClassCastException if there is a preference with this name
     * that is not a Set.
     *
     * @throws ClassCastException
     */
    Set<String> getStringSet(String key, Set<String> defValues);

    /**
     * Retrieve an int value from the preferences.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     *
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * an int.
     *
     * @throws ClassCastException
     */
    int getInt(String key, int defValue);

    /**
     * Retrieve a long value from the preferences.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     *
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a long.
     *
     * @throws ClassCastException
     */
    long getLong(String key, long defValue);

    /**
     * Retrieve a float value from the preferences.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     *
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a float.
     *
     * @throws ClassCastException
     */
    float getFloat(String key, float defValue);

    /**
     * Retrieve a boolean value from the preferences.
     *
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     *
     * @return Returns the preference value if it exists, or defValue.  Throws
     * ClassCastException if there is a preference with this name that is not
     * a boolean.
     *
     * @throws ClassCastException
     */
    boolean getBoolean(String key, boolean defValue);

    /**
     * Checks whether the preferences contains a preference.
     *
     * @param key The name of the preference to check.
     * @return Returns true if the preference exists in the preferences,
     *         otherwise false.
     */
    boolean contains(String key);

    /**
     * Create a new Editor for these preferences, through which you can make
     * modifications to the data in the preferences and atomically commit those
     * changes back to the SharedPreferences object.
     *
     * <p>Note that you <em>must</em> call {@link Editor#commit} to have any
     * changes you perform in the Editor actually show up in the
     * SharedPreferences.
     *
     * @return Returns a new instance of the {@link Editor} interface, allowing
     * you to modify the values in this SharedPreferences object.
     */
    EditorCompat edit();

    /**
     * Registers a callback to be invoked when a change happens to a preference.
     *
     * @param listener The callback that will run.
     * @see #unregisterOnSharedPreferenceChangeListener
     */
    void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener);

    /**
     * Unregisters a previous callback.
     *
     * @param listener The callback that should be unregistered.
     * @see #registerOnSharedPreferenceChangeListener
     */
    void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener);

	public interface EditorCompat extends SharedPreferences.Editor {
		@Override public void apply();
	}
}

@NonNullByDefault
class SharedPreferencesWrapper implements SharedPreferencesCompat {

	SharedPreferencesWrapper(android.content.SharedPreferences prefs) { delegatee = prefs; }

	@Override public EditorCompat edit() {
		return new EditorCompatWrapper(delegatee.edit());
	}

	@SuppressWarnings("null")	// The nullness of return value depends on parameter defValues
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override public Set<String> getStringSet(String key, Set<String> defValues) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
			return delegatee.getStringSet(key, defValues);
		String joined = delegatee.getString(key, null);
		if (joined == null) return defValues;
		if (joined.length() == 0) return Collections.emptySet();
		return new HashSet<String>(Arrays.asList(joined.split(String.valueOf(STRING_SET_COMPAT_SEPARATOR))));
	}

	@SuppressWarnings("null")	// SDK lacks @NonNull declaration
	@Override public Map<String, ?> getAll() { return delegatee.getAll(); }
	@SuppressWarnings("null")	// The nullness of return value depends on parameter defValues
	@Override public String getString(String key, String defValue) { return delegatee.getString(key, defValue); }
	@Override public int getInt(String key, int defValue) { return delegatee.getInt(key, defValue); }
	@Override public long getLong(String key, long defValue) { return delegatee.getLong(key, defValue); }
	@Override public float getFloat(String key, float defValue) { return delegatee.getFloat(key, defValue); }
	@Override public boolean getBoolean(String key, boolean defValue) { return delegatee.getBoolean(key, defValue); }
	@Override public boolean contains(String key) { return delegatee.contains(key); }
	@Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { delegatee.registerOnSharedPreferenceChangeListener(listener); }
	@Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { delegatee.unregisterOnSharedPreferenceChangeListener(listener); }

	private final android.content.SharedPreferences delegatee;
}

class EditorCompatWrapper implements EditorCompat {

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override public void apply() {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD)
			delegatee.apply();
		else delegatee.commit();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override public Editor putStringSet(String key, Set<String> values) {
		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
			return delegatee.putStringSet(key, values);
		// Use string with separator to store string set.
		StringBuilder joiner = new StringBuilder();
		for (String value : values)
			joiner.append(SharedPreferencesCompat.STRING_SET_COMPAT_SEPARATOR).append(value);
		return delegatee.putString(key, joiner.substring(1));
	}

	public EditorCompatWrapper(Editor edit) { delegatee = edit; }
	@Override public Editor putString(String key, String value) { return delegatee.putString(key, value); }
	@Override public Editor putInt(String key, int value) { return delegatee.putInt(key, value); }
	@Override public Editor putLong(String key, long value) { return delegatee.putLong(key, value); }
	@Override public Editor putFloat(String key, float value) { return delegatee.putFloat(key, value); }
	@Override public Editor putBoolean(String key, boolean value) { return delegatee.putBoolean(key, value); }
	@Override public Editor remove(String key) { return delegatee.remove(key); }
	@Override public Editor clear() { return delegatee.clear(); }
	@Override public boolean commit() { return delegatee.commit(); }

	private final Editor delegatee;
}
