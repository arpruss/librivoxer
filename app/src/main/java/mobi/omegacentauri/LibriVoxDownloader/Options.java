package mobi.omegacentauri.LibriVoxDownloader;

import mobi.omegacentauri.LibriVoxDownloader.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Options extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String PREF_CURRENT_LIST = "currentList";
	public static final String PREF_SELECTED_ITEM_PREFIX = "selectedItem";
	public static final String PREF_FORMAT = "format";
//	public static final String PREF_BASE = "Librivox";
	public static final String OPT_OGG = "ogg";
	public static final String OPT_MP3 = "mp3";
	public static final String PREF_UPDATE_TRIED = "updateTried";
	public static final String PREF_UPDATE_SUCCEEDED = "updateSucceeded";
	public static final String PREF_ONLY_INSTALLED = "onlyInstalled";
	public static final String PREF_PLAY = "playButton";
	public static final String OPT_PLAYLIST = "playlist";
	public static final String OPT_LAUNCH = "launch";
	public static final String PREF_ID = "id";
	public static final String PREF_RETRIES = "retries";
	public static final String PREF_DATABASE_CURRENT_TO = "dbCurrentTo";
    public static final String PREF_FOLDER_URI = "folderUri";//for SDK 19 and above

    private static String[] summaryKeys = { /*PREF_PLAY,*/ PREF_RETRIES };
	private static int[] summaryEntryValues = { /*R.array.play_buttons,*/ R.array.retries };
	private static int[] summaryEntryLabels = { /*R.array.play_button_labels,*/ R.array.retries };
	private static String[] summaryDefaults = { /*OPT_PLAYLIST,*/ "2" };
	
	public static String getString(SharedPreferences options, String key) {
	for (int i=0; i<summaryKeys.length; i++)
		if (summaryKeys[i].equals(key)) 
			return options.getString(key, summaryDefaults[i]);
	
	return options.getString(key, "");
}

@Override
public void onCreate(Bundle icicle) {
	super.onCreate(icicle);

	addPreferencesFromResource(R.xml.options);
}

@Override
public void onResume() {
	super.onResume();

	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	setSummaries();
}

public void setSummaries() {
	for (int i=0; i<summaryKeys.length; i++) {
		setSummary(i);
	}
}

public void setSummary(String key) {		
	for (int i=0; i<summaryKeys.length; i++) {
		if (summaryKeys[i].equals(key)) {
			setSummary(i);
			return;
		}
	}
}

public void setSummary(int i) {
	SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(this);
	Resources res = getResources();
	
	Preference pref = findPreference(summaryKeys[i]);
	String value = options.getString(summaryKeys[i], summaryDefaults[i]);
	
	String[] valueArray = res.getStringArray(summaryEntryValues[i]);
	String[] entryArray = res.getStringArray(summaryEntryLabels[i]);
	
	for (int j=0; j<valueArray.length; j++) 
		if (valueArray[j].equals(value)) {
			pref.setSummary(entryArray[j]);
			return;
		}
}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences options, String key) {
		setSummary(key);
	}
}
