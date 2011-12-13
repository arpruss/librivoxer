package mobi.omegacentauri.Librivoxer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Browser extends Activity {
	private static final String ALL = "All";
	private static final String AUTHORS = "Authors"; 
	private static final String GENRES = "Genres";
	private static final String[] topList = { ALL, AUTHORS, GENRES };
	private static final int NUM_LISTS = 3;
	private int currentList;
	private String[] selectedItem;
	private SQLiteDatabase db;
	private ListView listView;
	private Cursor cursor;
	private boolean onlyInstalled;
	private String[] curItems;
	
	SharedPreferences options;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        curItems = null;
        selectedItem = new String[NUM_LISTS];
        options = PreferenceManager.getDefaultSharedPreferences(this);
        
        for (int i=0; i < NUM_LISTS; i++)
        	selectedItem[i] = options.getString(Options.PREF_SELECTED_ITEM_PREFIX+i, "");
        
        currentList = options.getInt(Options.PREF_CURRENT_LIST, 0);
        
        setContentView(R.layout.main);

        listView = (ListView)findViewById(R.id.list); 

        CheckBox cb = (CheckBox)findViewById(R.id.installed);
        onlyInstalled = options.getBoolean(Options.PREF_ONLY_INSTALLED, false);
        cb.setChecked(onlyInstalled);
        cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton button, boolean value) {
				onlyInstalled = value;
				SharedPreferences.Editor ed = options.edit();
				ed.putBoolean(Options.PREF_ONLY_INSTALLED, value);
				ed.commit();
				populateList();
			}});
    	
        
        createDBFromSplit();
//        createDBFromXML();
        Log.v("Book", "-onCreate");
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
    	db = Book.getDB(this); 
    	
    	populateList();		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		closeCursor(); // TODO: deactivate, not close
		SharedPreferences.Editor ed = options.edit();
		ed.putInt(Options.PREF_CURRENT_LIST, currentList);
        for (int i=0; i < NUM_LISTS; i++)
        	ed.putString(Options.PREF_SELECTED_ITEM_PREFIX+i, selectedItem[i]);
        ed.commit();
        db.close();
	}
	
	synchronized void populateList() {
		switch (currentList) {
		case 0:
			setArrayList(topList);
			break;
		case 1:
			if (selectedItem[0].equals(GENRES)) {
				setArrayList(Book.standardGenres); // TODO: allow extensions
			}
			else {
				new PopulateListTask(false).execute();
			}
			break;
		case 2:
			new PopulateListTask(false).execute();
			break;
		}
	}
	
	private void arrayListSelect(int position) {
		String text = (String)listView.getAdapter().getItem(position);
		Log.v("Book", "Select "+text);
		selectedItem[currentList] = text;
		currentList++;
		populateList();
	}
	
	private void cursorListSelect(int position) {
		cursor.moveToPosition(position);
		Intent i = new Intent(this, ItemView.class);
		int id = cursor.getInt(0);
		Log.v("Book", "id = "+id);
		i.putExtra(Book.DBID, id);
		startActivity(i);
	}
	
	private void closeCursor() {
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}
	
	private void setCursorList(Cursor cursor) {
		final int author;
		final int author2;
		final int title;
		
		curItems = null;
		
		author = cursor.getColumnIndex(Book.AUTHOR);
		author2 = cursor.getColumnIndex(Book.AUTHOR2);
		title = cursor.getColumnIndex(Book.TITLE);

		closeCursor();
		this.cursor = cursor;
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				cursorListSelect(position);				
			}        	
		});


		CursorAdapter adapter = new CursorAdapter(this, cursor){

			@Override
			public void bindView(View view, Context context, Cursor cursor) {

				String authors = cursor.getString(author);
				String a2 = cursor.getString(author2);
				if (a2.length() > 0) {
					authors += " & "+a2;
				}

				((TextView)view.findViewById(R.id.text1))
				.setText(new SpannableString(authors));
				((TextView)view.findViewById(R.id.text2))
				.setText(new SpannableString(cursor.getString(title)));
			}

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				View v = View.inflate(Browser.this, R.layout.twoline, null);
				bindView(v, context, cursor);
				return v;
			}};
			listView.setAdapter(adapter); 
	}

	private void setArrayList(final String[] list) {
		Log.v("Book", "Set array list "+list.length);
		curItems = list;
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				arrayListSelect(position);				
			}        	
		});

		listView.setAdapter(new ArrayAdapter<String>(this, R.layout.oneline, list) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v;				
			
			if (convertView == null) {
                v = View.inflate(Browser.this, R.layout.oneline, null);
            }
			else {
				v = convertView;
			}

			((TextView)v.findViewById(R.id.text1))
				.setText(new SpannableString( list[position]));
			return v;
		}				
	});
	}
	
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
    		if (currentList > 0) {
    			currentList--;
    			populateList();
    		}
    		else {
    			finish();
    		}
			return true;
    	}
		return super.dispatchKeyEvent(event);    	
    };



    private void createDBFromSplit() {
    	File dbFile = new File(Book.getDBPath(this)); 
    	if (dbFile.exists() && dbFile.length() > 2000000)
    		return;

    	OutputStream out;

		try {
			out = new FileOutputStream(dbFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "Cannot create database", 2000).show();
			finish();
			return;
		}
    	
    	AssetManager assets = getAssets();
    	
    	boolean done = false;
    	
		for (int i=0; i<100 & !done; i++ ) {
    		try {
    			Log.v("Book", "opening "+i);
    			InputStream in = assets.open("booksdb0"+i);
    			if (!copyStream(in, out)) {
    				out.close();
    				dbFile.delete();
    				Toast.makeText(this, "Cannot create database", 2000).show();
    				finish();
    			}
    		}
    		catch (IOException e) {
    			done = true;
    		}
    	}   
    }
    
    public static boolean copyStream(InputStream in, OutputStream out) {
    	final int BUFFER_SIZE = 16384;
    	byte[] buffer = new byte[BUFFER_SIZE];
    	
    	int didRead;
    	
    	try {
			while((didRead = in.read(buffer)) > 0) {
				out.write(buffer, 0, didRead);
			}
		} catch (IOException e) {
			return false;
		}
		
		return true;
    }
    
    private void createDBFromXML() {
    	File dbFile = new File(Book.getDBPath(this));
    	if (dbFile.exists())
    		dbFile.delete();

    	db = Book.getDB(this, true);
    	Book.createTable(db);
    	boolean done = false;
    	for (int i=1; i<100 & !done; i++ ) {
    		try {
    			Log.v("Book", "parsing "+i);
    			ParseToDB pa = new ParseToDB(getAssets().open("catalog"+i+".xml"), db);
    			pa.parse(false);
    		}
    		catch (IOException e) {
    			done = true;
    		}
    	}
    	db.close();
    }

    private class PopulateListTask extends AsyncTask<Void, Integer, Cursor> {
    	ProgressDialog progress;
    	static final int UPDATING = 0;
    	static final int SEARCHING = 1;
    	private boolean ignoreError;
    	private boolean forceUpdate;
    	int updated = 0;
    	
    	public PopulateListTask(boolean forceUpdate) {
    		this.forceUpdate = forceUpdate;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		progress = new ProgressDialog(Browser.this);
    		progress.setCancelable(false);
    		progress.show();
    		ignoreError = false;
    	}

    	protected void onProgressUpdate(Integer... p) {
    		if (p[0] == UPDATING)
    			progress.setMessage("Updating database...");
    		else if (p[0] == SEARCHING)
    			progress.setMessage("Searching...");
    	}
    	
    	
    	void updateDB() {
    		if (!forceUpdate) {
	    		if (System.currentTimeMillis() < 7l * 86400l * 1000l + 
	    				options.getLong(Options.PREF_UPDATE_SUCCEEDED, 0))
	    			return;
	    		if (System.currentTimeMillis() < 2l * 3600l * 1000l + 
	    				options.getLong(Options.PREF_UPDATE_TRIED, 0))
	    			return;
    		}
    		
    		publishProgress(UPDATING);

    		try {
				SharedPreferences.Editor ed = options.edit();
				ed.putLong(Options.PREF_UPDATE_TRIED, System.currentTimeMillis());
				ed.commit();
				
				int added = 0;
				for(int pos = 0;; pos+=50) {
					URL url;
					
					url = new URL("https://catalog.librivox.org/latestworks.xml?offset="+pos+"&limit=50"); 
					InputStream stream = url.openStream();
					ParseToDB parser = new ParseToDB(stream, db);
					if (!parser.parse(true)) {
						throw new IOException("parsing");
					}					
					added += parser.getAdded();
					if (parser.didHitOld() || 0 == parser.getAdded())
						break;
				}
				
				ed = options.edit();
				ed.putLong(Options.PREF_UPDATE_SUCCEEDED, System.currentTimeMillis());
				ed.commit();
				updated = added;
			} catch (MalformedURLException e) {
				updated = -1;
				Log.v("Book", "Update "+e);
			} catch (IOException e) {
				updated = -1;
				Log.v("Book", "Update "+e);
			}    		
    	}

		@Override
		protected Cursor doInBackground(Void... arg0) {
			updateDB();
			
			publishProgress(SEARCHING);
			switch(currentList) {
			case 1: 
				if (selectedItem[0].equals(AUTHORS)) {
					return Book.queryAuthors(db, onlyInstalled);
				}
				else if (selectedItem[0].equals(ALL)) { 
					return Book.queryAll(db, onlyInstalled);
				}
				else {
					ignoreError = true;
					return null;
				}
			case 2:
				if (selectedItem[0].equals(GENRES)) {
					return Book.queryGenre(db, selectedItem[1], onlyInstalled);
				}
				else if (selectedItem[0].equals(AUTHORS)) {
					return Book.queryAuthor(db, selectedItem[1], onlyInstalled);
				}
				else {
					ignoreError = true;
					return null;
				}
			}
			ignoreError = true;
			return null;
		}
		
		@Override
		protected void onPostExecute(Cursor cursor) {
			progress.dismiss();
			Log.v("Book", "updated "+updated);
			if (updated > 0) {
				Toast.makeText(Browser.this, "Added "+updated+
						(updated > 1 ? " items": " item") + " to database", 3000).show();
			}
			else if (updated < 0) {
				Toast.makeText(Browser.this, "Database update unsuccessful", 3000).show();
			}
			
			if (cursor == null) {
				if (!ignoreError) {
					Toast.makeText(Browser.this, "Error searching", 3000).show();
					currentList = 0;
					populateList();
				}
			}
			else {
				Log.v("Book", "cl="+currentList+" si="+selectedItem[0]);
				if (currentList == 1 && selectedItem[0].equals(AUTHORS)) {
					setArrayList(cursorToArray(cursor));
				}
				else {
					setCursorList(cursor);
				}
			}
		}
    }

	private static String[] cursorToArray(Cursor cursor) {
		int count = cursor.getCount();
		String[] s = new String[count];
		cursor.moveToFirst();
		for (int i=0; i<count; i++) {
			s[i] = cursor.getString(0);
			cursor.moveToNext();
		}
		return s;
    }

	private void fatalError(String title, String msg) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		Log.e("Lunar", "fatal: "+title);

		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {finish();} });
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {finish();} });
		alertDialog.show();		
	}

	
	private void license() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.setTitle("License");
		alertDialog.setMessage(Html.fromHtml(Utils.getAssetString(getAssets(), "licenses.txt")));
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {finish();} });
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {finish();} });
		alertDialog.show();		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.update:
			new PopulateListTask(true).execute();
			return true;
    	case R.id.license:
    		license();
    		return true;
    	case R.id.options:
			startActivity(new Intent(this, Options.class));			
			return true;
    	}
    	return false;
    	
    }
}
