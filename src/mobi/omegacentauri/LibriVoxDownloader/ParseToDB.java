package mobi.omegacentauri.LibriVoxDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Xml;

public class ParseToDB implements BookSaver {
	private InputStream stream;
	private String xmlData;
	private SQLiteDatabase db;
	private boolean hitOld;
	private boolean updateOnly;
	private int added;
	
	public ParseToDB(InputStream stream, SQLiteDatabase db) {
		this.stream = stream;
		this.db = db;
		this.xmlData = null;
	}
	
	public ParseToDB(String xmlData, SQLiteDatabase db) {
		this.stream = null;
		this.db = db;
		this.xmlData = xmlData;
	}
	
	public boolean didHitOld() {
		return hitOld;
	}
	
	public boolean parse(boolean updateOnly) {
		hitOld = false;
		added = 0;
		this.updateOnly = updateOnly;
		BookHandler handler = new BookHandler(this);
		try {
			if (stream != null)
				Xml.parse(stream, Xml.Encoding.UTF_8, handler);
			else
				Xml.parse(xmlData, handler);
			return true;
		} catch (IOException e) {
			Log.v("Book", "error "+e);
			return false;
		} catch (SAXException e) {
			Log.v("Book", "error "+e);
			return false;
		}
	}
	
	public void saveStart() {
	}

	public void saveBook(Book book) {
		if (updateOnly && book.existsInDB(db)) {
			hitOld = true;
		}
		else {
			added++;
			book.saveToDB(db);
			
			if (updateOnly)
				Log.v("Book", "add:"+book.title);
		}
	}

	public void saveDone() {
	}

	public int getAdded() {
		return added;
	}
}
