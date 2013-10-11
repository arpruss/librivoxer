package mobi.omegacentauri.LibriVoxDownloader;

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

import mobi.omegacentauri.LibriVoxDownloader.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnKeyListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Browser extends Activity {
	private static final long DATABASE_UPDATED_TO = 1323410400;
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
	private EditText searchBox;
	private Button searchButton;
	private boolean fastSearch;
	
	SharedPreferences options;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        fastSearch = false;
        curItems = null;
        selectedItem = new String[NUM_LISTS];
        options = PreferenceManager.getDefaultSharedPreferences(this);
        
        for (int i=0; i < NUM_LISTS; i++)
        	selectedItem[i] = options.getString(Options.PREF_SELECTED_ITEM_PREFIX+i, "");
        
        currentList = options.getInt(Options.PREF_CURRENT_LIST, 0);
        
        setContentView(R.layout.browser);

        listView = (ListView)findViewById(R.id.list); 
        searchButton = (Button)findViewById(R.id.search_button);
        searchButton.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View view) {
				searchClick(view);				
			}});
        searchBox = (EditText)findViewById(R.id.search_box);
		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable view) {
				if (fastSearch) {
					doFastSearch();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}});
		
        

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
        
        new PleaseBuy(this,false);
    }
	
	boolean inAuthorSearch() {
		return currentList == 1 && selectedItem[0].equals(AUTHORS);
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
		
		try {
			db = Book.getDB(this);
		}
		catch (SQLiteException e) {
			Toast.makeText(this, "Cannot open db: contact developer", 5000).show();
			finish();
			return;
		}
    	
    	populateList();		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		if (db != null) {
			closeCursor(); // TODO: deactivate, not close
			SharedPreferences.Editor ed = options.edit();
			ed.putInt(Options.PREF_CURRENT_LIST, currentList);
	        for (int i=0; i < NUM_LISTS; i++)
	        	ed.putString(Options.PREF_SELECTED_ITEM_PREFIX+i, selectedItem[i]);
	        ed.commit();
	        db.close();

	        db = null;
		}
	}
	
	synchronized void doFastSearch() {
		String data = searchBox.getText().toString().trim().toLowerCase();

		if(data.length() == 0) {
			setArrayListNoCurItems(curItems);
			return;
		}		

		ArrayList<String> list = new ArrayList<String>();
		
		for (String item: curItems) {
			if (item.toLowerCase().contains(data)) {
				list.add(item);
			}
		}
		
		String[] array = new String[list.size()];
		for (int i=0; i< list.size(); i++)
			array[i] = list.get(i);
		setArrayListNoCurItems(array);
	}
	
	synchronized void populateList() {
    	Log.v("Book", "populating");
    	
		fastSearch = false;
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		switch (currentList) {
		case 0:
			searchBox.setVisibility(View.VISIBLE);
			searchButton.setVisibility(View.VISIBLE);
			setArrayList(topList);
			break;
		case 1:
			Log.v("Book", "Level 1 "+selectedItem[0]);
			if (selectedItem[0].equals(GENRES)) {
				searchBox.setVisibility(View.GONE);
				searchButton.setVisibility(View.GONE);
				setArrayList(Book.standardGenres); // TODO: allow extensions
			}
			else {
				searchBox.setVisibility(View.VISIBLE);
				if (selectedItem[0].equals(AUTHORS)) {
					Log.v("Book", "noSB vis");
					searchButton.setVisibility(View.GONE);
					fastSearch = true;
				}
				else {
					searchButton.setVisibility(View.VISIBLE);
				}
					
				new PopulateListTask(false).execute();
			}
			break;
		case 2:
			searchBox.setVisibility(selectedItem[0].equals(AUTHORS) ? View.GONE : View.VISIBLE );
			searchButton.setVisibility(selectedItem[0].equals(AUTHORS) ? View.GONE : View.VISIBLE );
			new PopulateListTask(false).execute();
			break;
		}
		
		if (searchBox.getVisibility()==View.GONE)
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	
	public void searchClick(View view) {
		Log.v("Book", "searchClick");
		if (currentList == 0) {
			selectedItem[0] = ALL;
			currentList = 1;
		}
		populateList();
	}
	
	private void arrayListSelect(int position) {
		String text = (String)listView.getAdapter().getItem(position);
		Log.v("Book", "Select "+text);
		selectedItem[currentList] = text;
		currentList++;
		populateList();
		searchBox.setText("");
	}
	
	private void cursorListSelect(int position) {
		cursor.moveToPosition(position);
		SharedPreferences.Editor ed = options.edit();
		ed.putInt(Options.PREF_ID, cursor.getInt(0));
		ed.commit();
		cursor.close();
		startActivity(new Intent(this, ItemView.class));
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

	private void setArrayList(String[] list) {
		Log.v("Book", "Set array list "+list.length);
		curItems = list;
		
		if (fastSearch)
			doFastSearch();
		else
			setArrayListNoCurItems(list);
	}
	
	private void setArrayListNoCurItems(final String[] list) {		
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
			if (searchBox.getVisibility() == View.VISIBLE && 
					searchBox.getText().toString().length() > 0) {
				searchBox.setText("");
    			populateList();
			}
			else {
	    		if (currentList > 0) {
	    			currentList--;
	    			populateList();
	    		}
	    		else {
	    			finish();
	    		}
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
    	db = null;
    }

    private class PopulateListTask extends AsyncTask<Void, Integer, Cursor> {
    	ProgressDialog progress;
    	static final int UPDATING = 0;
    	static final int SEARCHING = 1;
    	private boolean ignoreError;
    	private boolean forceUpdate;
    	private String searchText;
    	int updated = 0;
    	
    	public PopulateListTask(boolean forceUpdate) {
    		Log.v("Book","PopulateListTask");
    		this.forceUpdate = forceUpdate;
    		if (searchBox.getVisibility()==View.VISIBLE &&
    				searchBox.getText().length()>0)
    			searchText = searchBox.getText().toString().trim();
    		else
    			searchText = null;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		Log.v("Book","PopulateListTask.onPreExecute");
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
    	
    	
    	private void updateDB() {
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
				long triedAt = System.currentTimeMillis();
				
				ed.putLong(Options.PREF_UPDATE_TRIED, triedAt);
				ed.commit();
				
				// subtract a week from the time of last update in case device's clock
				// was off
				URL url = new URL("http://librivox.org/api/feed/audiobooks/?since="+
							(options.getLong(Options.PREF_DATABASE_CURRENT_TO, DATABASE_UPDATED_TO)-7*86400)+
							"&limit=999999");
				Log.v("Book","updating "+url);
				InputStream stream = url.openStream();
				ParseToDB parser = new ParseToDB(stream, db);
				if (!parser.parse(true)) {
					throw new IOException("parsing");
				}					
				updated = parser.getAdded();
				
				ed = options.edit();
				ed.putLong(Options.PREF_UPDATE_SUCCEEDED, System.currentTimeMillis());
				ed.putLong(Options.PREF_DATABASE_CURRENT_TO, triedAt/1000);
				ed.commit();
				Log.v("Book", "finished updating");
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
			if (db == null)
				return null;
			
			updateDB();
			
			publishProgress(SEARCHING);
			switch(currentList) {
			case 1: 
				if (selectedItem[0].equals(AUTHORS)) {
					return Book.queryAuthors(db, onlyInstalled);
				}
				else if (selectedItem[0].equals(ALL)) { 
					return Book.queryAll(db, onlyInstalled, searchText);
				}
				else {
					ignoreError = true;
					return null;
				}
			case 2:
				if (selectedItem[0].equals(GENRES)) {
					return Book.queryGenre(db, selectedItem[1], onlyInstalled, searchText);
				}
				else if (selectedItem[0].equals(AUTHORS)) {
					return Book.queryAuthor(db, selectedItem[1], onlyInstalled, searchText);
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
					cursor.close();
					closeCursor();
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
		license(this);
	}
	
	public static void license(Context context) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		alertDialog.setTitle("License");
		alertDialog.setMessage(Html.fromHtml(Utils.getAssetString(context.getAssets(), "licenses.txt")));
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {} });
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {} });
		alertDialog.show();		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.please_buy:
    		pleaseBuy(true);
    		return true;
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
	
	private void pleaseBuy(boolean always) {
		new PleaseBuy(this, always);
	}

//	public static void pleaseBuy(final Context c, boolean always) {
//		if (!always) {
//			SharedPreferences p = c.getSharedPreferences("PleaseBuy", 0);
//			int v;
//			try {
//				v = c.getPackageManager()
//					.getPackageInfo(c.getPackageName(),0).versionCode;
//			} catch (NameNotFoundException e) {
//				v = 0;
//			}
//			if (p.getInt("version", 0) == v) {
//				return;
//			}
//			SharedPreferences.Editor ed = p.edit();
//			ed.putInt("version", v);
//			ed.commit();
//		}
//		
//        AlertDialog alertDialog = new AlertDialog.Builder(c).create();
//
//        alertDialog.setTitle("Other applications?");
//        
//        alertDialog.setMessage("Do you wish to visit the "+Market+" "+
//        		"to find other applications from Omega Centauri Software?  You will "+
//        		"be able to return to SuperDim with the BACK button.  (You will "+
//        		"only be asked this once when you install a new version, but you "+
//        		"can always come back to this option by pulling up the menu.)");
//        
//        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
//        		"See other apps", 
//        	new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//            	Intent i = new Intent(Intent.ACTION_VIEW);
//            	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            	if (Market.contains("arket"))
//            		i.setData(Uri.parse("market://search?q=pub:\"Omega Centauri Software\""));
//            	else
//            		i.setData(Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=mobi.pruss.force2sd&showAll=1"));            		
//            	c.startActivity(i);
//            } });
//        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, 
//        		"Not now", 
//        	new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {} });
//        alertDialog.show();				
//	}

}
