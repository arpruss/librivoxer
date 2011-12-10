package mobi.omegacentauri.Librivoxer;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class Book {
	// These tags are both for xml and sqlite
	
	public static final String DB_FILENAME = "books.db";
	
	public static final String BOOK_TABLE = "tbl_books";
	public static final String TITLE = "title";
	public String title = "";
	public static final String AUTHOR = "author";
	public String author = "";
	public static final String AUTHOR2 = "author2";
	public String author2 = "";
	public static final String ETEXT = "etext";
	public String etext = "";
	public static final String CATEGORY = "category";
	public String category = "";
	public static final String GENRE = "genre";
	public String genre = "";
	public static final String LANGUAGE = "language";
	public String language = "";
	public static final String RSSURL = "rssurl";
	public String rssurl = "";
	public static final String TRANSLATOR = "translator";
	public String translator = "";
	public static final String COPYRIGHTYEAR = "copyrightyear";
	public String copyrightyear = "";
	public static final String TOTALTIME = "totaltime";
	public String totaltime = "";
	public static final String COMPLETED = "completed";
	public String completed = "";
	public static final String DESCRIPTION = "description";
	public String description = "";
	public static final String ID = "id";
	public int id;
	
	public Book() {
		id = -1;
	}
	
	public void saveToDB(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(ID, id);
		values.put(AUTHOR, author);
		values.put(AUTHOR2, author2);
		values.put(CATEGORY, category);
		values.put(COMPLETED, completed);
		values.put(COPYRIGHTYEAR, copyrightyear);
		values.put(DESCRIPTION, description);
		values.put(ETEXT, etext);
		values.put(GENRE, genre);
		values.put(LANGUAGE, language);
		values.put(RSSURL, rssurl);
		values.put(TITLE, title);
		values.put(TOTALTIME, totaltime);
		values.put(TRANSLATOR, translator);
		db.insert(BOOK_TABLE, null, values);
	}
	
	public static void createTable(SQLiteDatabase db) {
    	db.execSQL("CREATE TABLE "+BOOK_TABLE+" ("+
				Book.ID+" INTEGER PRIMARY KEY,"+
				Book.AUTHOR+" TEXT,"+
				Book.AUTHOR2+" TEXT,"+
				Book.CATEGORY+" TEXT,"+
				Book.COMPLETED+" TEXT,"+
				Book.COPYRIGHTYEAR+" TEXT,"+
				Book.DESCRIPTION+" TEXT,"+
				Book.ETEXT+" TEXT,"+
				Book.GENRE+" TEXT,"+
				Book.LANGUAGE+" TEXT,"+
				Book.RSSURL+" TEXT,"+
				Book.TITLE+" TEXT,"+
				Book.TOTALTIME+ " TEXT,"+
				Book.TRANSLATOR+ " TEXT);");		
	}
}
