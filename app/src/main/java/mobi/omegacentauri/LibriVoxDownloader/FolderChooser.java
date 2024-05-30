package mobi.omegacentauri.LibriVoxDownloader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FolderChooser extends ListActivity {
	public static final int NEW_FOLDER_REQUEST_CODE = 500;
	private SharedPreferences options;
    private File currentFolder;
	private File parentFolder;

	public static void choose(Activity a) {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("content://com.android.externalstorage.documents/document/primary%3ALibriVox"));
		a.startActivityForResult(intent, NEW_FOLDER_REQUEST_CODE);
		return;
	}

	public static void result(Activity a, int requestCode, int resultCode, Intent data) {
		if (NEW_FOLDER_REQUEST_CODE == requestCode && resultCode == RESULT_OK) {
			Uri uri = null;
			if (data != null) {
				uri = data.getData();
				if (uri != null) {
					a.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
					SharedPreferences o = PreferenceManager.getDefaultSharedPreferences(a);
					o.edit().putString(Options.PREF_FOLDER_URI, uri.toString()).apply();
				}
			}
		}
	}
}
