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
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Browser extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        createDBFromAssets();
    }
    
    
    void createDBFromAssets() {
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
    
    boolean copyStream(InputStream in, OutputStream out) {
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
    
    void parseToList() {
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
    
    void createDB() {
    	SQLiteDatabase db = openOrCreateDatabase("books.db", 
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
