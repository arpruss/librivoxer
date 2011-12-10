package mobi.omegacentauri.Librivoxer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Browser extends Activity {
	private static final String ALL = "All";
	private static final String AUTHOR = "Authors"; 
	private static final String GENRE = "Genres";
	private static final String[] topList = { ALL, AUTHOR, GENRE };
	private static final int NUM_LISTS = 3;
	private int currentList;
	private String[] selectedItem;
	private SQLiteDatabase db;
	private ListView listView;

	
	SharedPreferences options;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        selectedItem = new String[NUM_LISTS];
        options = PreferenceManager.getDefaultSharedPreferences(this);
        
        for (int i=0; i < NUM_LISTS; i++)
        	selectedItem[i] = options.getString(Options.PREF_SELECTED_ITEM_PREFIX+i, "");
        
        currentList = options.getInt(Options.PREF_CURRENT_LIST, 0);
        
        setContentView(R.layout.main);

        listView = (ListView)findViewById(R.id.list); 
        
        createDBFromSplit();
//        createDBFromXML();
        
    	db = openOrCreateDatabase(Book.DB_FILENAME,
    			SQLiteDatabase.CREATE_IF_NECESSARY, null);    	
    	
    	populateList();
    }
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences.Editor ed = options.edit();
		ed.putInt(Options.PREF_CURRENT_LIST, currentList);
        for (int i=0; i < NUM_LISTS; i++)
        	ed.putString(Options.PREF_SELECTED_ITEM_PREFIX+i, selectedItem[i]);
        ed.commit();        
	}
	
	void populateList() {
		switch (currentList) {
		case 0:
			setArrayList(topList);
			break;
		case 1:
			if (selectedItem[0].equals(GENRE)) {
				setArrayList(Book.standardGenres); // TODO: allow extensions
			}
			else if (selectedItem[0].equals(AUTHOR)) {
				setArrayList(Book.getAuthors(db));
			}
			else if (selectedItem[0].equals(ALL)) {
//				setDBList("select * from "+Book.BOOK_TABLE);
			}
			break;
		case 2:
			if (selectedItem[0].equals(GENRE))
				setCursorList(Book.queryGenre(db, selectedItem[0]));
			else if (selectedItem[0].equals(AUTHOR))
				setCursorList(Book.queryAuthor(db, selectedItem[1]));
		}
	}
	
	private void select(int position) {
		String text = (String)listView.getAdapter().getItem(position);
		switch (currentList) {
		case 0:
			Log.v("Book", "Select "+text);
			selectedItem[0] = text;
			currentList++;
			populateList();
			break;
		}
	}
	
	private void setArrayList(final String[] list) {
		Log.v("Book", "Set array list "+list.length);
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
				.setText(list[position]);
			return v;
		}				
	});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				select(position);				
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
    	File dbFile = getDatabasePath(Book.DB_FILENAME);
    	if (dbFile.exists())
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
    
    private void parseToList() {
    	boolean done = false;
    	ArrayList<Book> books = new ArrayList<Book>();
		Log.v("Book", "parse");
    	
    	for (int i=1; i<100 & !done; i++ ) {
    		try {
    			Log.v("Book", "parsing "+i);
    			ParseToList pa = new ParseToList(getAssets().open("catalog"+i+".xml"), books);
    			pa.parse();
    	    	Log.v("Book", "count: "+books.size());
    		}
    		catch (IOException e) {
    			done = true;
    		}
    	}
		
    	Log.v("Book", "count: "+books.size());
    }
    
    private void createDBFromXML() {
    	File dbFile = getDatabasePath(Book.DB_FILENAME);
    	if (dbFile.exists())
    		dbFile.delete();

    	SQLiteDatabase db = openOrCreateDatabase(Book.DB_FILENAME,
    			SQLiteDatabase.CREATE_IF_NECESSARY, null);
    	Book.createTable(db);
    	boolean done = false;
    	for (int i=1; i<100 & !done; i++ ) {
    		try {
    			Log.v("Book", "parsing "+i);
    			ParseToDB pa = new ParseToDB(getAssets().open("catalog"+i+".xml"), db);
    			pa.parse();
    		}
    		catch (IOException e) {
    			done = true;
    		}
    	}
    	db.close();
    }
}
