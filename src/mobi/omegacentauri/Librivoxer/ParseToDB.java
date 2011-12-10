package mobi.omegacentauri.Librivoxer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.database.sqlite.SQLiteDatabase;
import android.util.Xml;

public class ParseToDB implements BookSaver {
	private InputStream stream;
	private SQLiteDatabase db;
	
	public ParseToDB(InputStream stream, SQLiteDatabase db) {
		this.stream = stream;
		this.db = db;
	}
	
	public boolean parse() {
		BookHandler handler = new BookHandler(this);
		try {
			Xml.parse(stream, Xml.Encoding.UTF_8, handler);
			return true;
		} catch (IOException e) {
			return false;
		} catch (SAXException e) {
			return false;
		}
	}
	
	public void saveStart() {
	}

	public void saveBook(Book book) {
		book.saveToDB(db);
	}

	public void saveDone() {
	}
}
