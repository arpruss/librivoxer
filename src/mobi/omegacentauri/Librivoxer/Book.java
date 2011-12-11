package mobi.omegacentauri.Librivoxer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
	public static final String XMLGENRE = "genre";
	public static final String DBGENRE_PREFIX = "genre";
	public static final int MAX_GENRES = 16;
	public String[] genres = new String[MAX_GENRES];
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
	public static final String ZIPFILE = "zipfile";
	public String zipfile = "";
	public static final String DBID = "_id";
	public static final String XMLID = "id";
	public static final String INSTALLED = "installed";	
	public String installed = "";
	public int id;
	public static final String[] standardGenres = {
		"Adventure",
		"Advice",
		"Ancient Texts",
		"Animals",
		"Art",
		"Biography",
		"Children",
		"Classics (antiquity)",
		"Comedy",
		"Cookery",
		"Economics/Political Economy",
		"Epistolary fiction",
		"Erotica",
		"Essay/Short nonfiction",
		"Fairy tales",
		"Fantasy",
		"Fiction",
		"Historical Fiction",
		"History",
		"Holiday",
		"Horror/Ghost stories",
		"Humor",
//		"Humour",
		"Instruction",
		"Languages",
//		"Literatur",
		"Literature",
		"Memoirs",
		"Music",
		"Mystery",
		"Myths/Legends",
		"Nature",
		"Philosophy",
		"Play",
		"Poetry",
		"Politics",
		"Psychology",
		"Religion",
		"Romance",
		"Satire",
		"Science",
		"Science fiction",
		"Sea stories",
		"Short",
		"Short stories",
		"Spy stories",
		"Teen/Young adult",
		"Tragedy",
		"Travel",
		"War stories",
		"Westerns"		
	};
	public static String QUERY_COLS = DBID+","+AUTHOR+","+AUTHOR2+","+TITLE;
	
	public Book() {
		id = -1;
	}
	
	public void saveToDB(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(DBID, id);
		values.put(AUTHOR, author);
		values.put(AUTHOR2, author2);
		values.put(CATEGORY, category);
		values.put(COMPLETED, completed);
		values.put(COPYRIGHTYEAR, copyrightyear);
		values.put(DESCRIPTION, description);
		values.put(ETEXT, etext);
		for (int i=0; i<MAX_GENRES; i++) {
			values.put(DBGENRE_PREFIX+i, genres[i]);
		}
		values.put(LANGUAGE, language);
		values.put(RSSURL, rssurl);
		values.put(TITLE, title);
		values.put(TOTALTIME, totaltime);
		values.put(TRANSLATOR, translator);
		values.put(INSTALLED, installed);
		values.put(ZIPFILE, zipfile);
		db.insert(BOOK_TABLE, null, values);
	}
	
	public static void createTable(SQLiteDatabase db) {
		String create = "CREATE TABLE "+BOOK_TABLE+" ("+
				Book.DBID+" INTEGER PRIMARY KEY,"+
				Book.AUTHOR+" TEXT,"+
				Book.AUTHOR2+" TEXT,"+
				Book.CATEGORY+" TEXT,"+
				Book.COMPLETED+" TEXT,"+
				Book.COPYRIGHTYEAR+" TEXT,"+
				Book.DESCRIPTION+" TEXT,"+
				Book.ETEXT+" TEXT,";
		
		for (int i=0; i<MAX_GENRES; i++)
			create += (DBGENRE_PREFIX+i)+" TEXT,";
		
		create +=
				Book.LANGUAGE+" TEXT,"+
				Book.RSSURL+" TEXT,"+
				Book.TITLE+" TEXT,"+
				Book.TOTALTIME+ " TEXT,"+
				Book.TRANSLATOR+ " TEXT,"+
				Book.ZIPFILE+" TEXT,"+
				Book.INSTALLED+ " TEXT);";
				

         db.execSQL(create);
	}

	public void setGenresFromXML(String genre) {
		String[] genres = genre.split(",\\s*");
		setGenres(genres);
	}

	public void setGenres(String[] genres) {
		int i;
		for (i = 0; i < MAX_GENRES && i < genres.length; i++) {
			String g = genres[i].trim();
			if (g.equals("Literatur"))
				g = "Literature";
			else if (g.equals("Humour"))
				g = "Humor";
			int j = Arrays.binarySearch(standardGenres, g);
			if (0 <= j) {
				this.genres[i] = Integer.toString(j);
			}
			else {
				this.genres[i] = g;
			}
		}
		
		for (; i < MAX_GENRES ; i++)
			this.genres[i] = "";
	}

	public String[] getGenres() {
		return genres;
	}
	
	public static String abbreviateGenre(String genre) {
		int j = Arrays.binarySearch(standardGenres, genre);
		if (0 <= j) {
			return Integer.toString(j);
		}
		else {
			return genre;
		}
	}
	
	public static String getGenreColumns() {
		String cols = "";
		
		for (int i=0; i<MAX_GENRES; i++) {
			if (0<i)
				cols += ",";
			cols += DBGENRE_PREFIX+i;
		}
		
		return cols;
	}
	
	public static Cursor queryAuthors(SQLiteDatabase db) {
		String query = "SELECT "+AUTHOR+" FROM "+BOOK_TABLE+ 
		" UNION SELECT "+AUTHOR2+" FROM "+BOOK_TABLE+" WHERE "+ AUTHOR2 +"<>''";
		Log.v("Book", query);
		return db.rawQuery(query,
				new String[] {});
	}

	public static Cursor queryGenre(SQLiteDatabase db, String string) {
		String query = "SELECT "+QUERY_COLS+" FROM "+BOOK_TABLE+
		   " WHERE "+DatabaseUtils.sqlEscapeString(abbreviateGenre(string))+
		   " IN ("+getGenreColumns()+") ORDER BY "+AUTHOR+","+AUTHOR2+","+TITLE;
		Log.v("Book", query);
		return db.rawQuery(query, new String[]{});
	}

	public static Cursor queryAuthor(SQLiteDatabase db, String string) {
		String query = "SELECT "+QUERY_COLS+" FROM "+BOOK_TABLE+
		   " WHERE " + DatabaseUtils.sqlEscapeString(string)+ 
		   " IN ("+AUTHOR+","+AUTHOR2+") ORDER BY "+TITLE;
		Log.v("Book", query);
		return db.rawQuery(query, new String[]{ });
	}
	
	public static Map<String,String> loadEntry(SQLiteDatabase db, int id) {
		String query = "SELECT * FROM "+BOOK_TABLE+" WHERE "+DBID+"='"+id+"'";
		Log.v("Book", query);
		Cursor cursor = db.rawQuery(query, new String[] {});
		cursor.moveToFirst();
		int cols = cursor.getColumnCount();
		HashMap<String,String> map = new HashMap<String,String>();
		for (int i=0; i<cols; i++) {
			map.put(cursor.getColumnName(i), cursor.getString(i));
		}
		cursor.close();
		return map;
	}
	
	public static Cursor queryAll(SQLiteDatabase db) {
		String query = "SELECT "+QUERY_COLS+" FROM "+BOOK_TABLE + " ORDER BY "+AUTHOR+","+AUTHOR2+","+TITLE;
		Log.v("Book", query);
		return db.rawQuery(query, new String[]{});
	}

	public static SQLiteDatabase getDB(Context context) {
		return SQLiteDatabase.openDatabase(context.getDatabasePath(Book.DB_FILENAME).getPath(), 
    			null, SQLiteDatabase.OPEN_READONLY);
	}
}
