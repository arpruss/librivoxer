package mobi.omegacentauri.LibriVoxDownloader;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class Book {
	// These tags are both for xml and sqlite
	
	public static final String[] emptyStringArray = {};
	
	private static final String DB_FILENAME = "books.db";
	
	public static final String BOOK_TABLE = "tbl_books";
	public static final String TITLE = "title";
	public String title = "";
	public static final String AUTHORS = "authors";
	public Authors authors = null;
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
	public static final String ONLY_INSTALLED = INSTALLED + " <> ''";
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
	
	public Book(SQLiteDatabase db, int id) {
		String query = "SELECT * FROM "+BOOK_TABLE+" WHERE "+DBID+"='"+id+"'";
		Log.v("Book", query);
		Cursor cursor = db.rawQuery(query, new String[] {});
		cursor.moveToFirst();
		id = cursor.getInt(cursor.getColumnIndex(DBID));
		author = cursor.getString(cursor.getColumnIndex(AUTHOR));
		author2 = cursor.getString(cursor.getColumnIndex(AUTHOR2));
		category = cursor.getString(cursor.getColumnIndex(CATEGORY));
		completed = cursor.getString(cursor.getColumnIndex(COMPLETED));
		copyrightyear = cursor.getString(cursor.getColumnIndex(COPYRIGHTYEAR));
		description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
		etext = cursor.getString(cursor.getColumnIndex(ETEXT));
		for (int i=0; i<MAX_GENRES; i++)
			genres[i] = cursor.getString(cursor.getColumnIndex(DBGENRE_PREFIX+i));
		language = cursor.getString(cursor.getColumnIndex(LANGUAGE));
		rssurl = cursor.getString(cursor.getColumnIndex(RSSURL));
		title = cursor.getString(cursor.getColumnIndex(TITLE));
		totaltime = cursor.getString(cursor.getColumnIndex(TOTALTIME));
		translator = cursor.getString(cursor.getColumnIndex(TRANSLATOR));
		zipfile = cursor.getString(cursor.getColumnIndex(ZIPFILE));
		Log.v("Book", ""+cursor.getColumnIndex(INSTALLED));
		installed = cursor.getString(cursor.getColumnIndex(INSTALLED));
		cursor.close();
	}
	
	public boolean existsInDB(SQLiteDatabase db) {
		final String query = "SELECT 1 FROM "+BOOK_TABLE+ 
		" WHERE "+ DBID +"='"+id+"'";

		Cursor cursor = db.rawQuery(query, emptyStringArray);
		if (cursor == null)
			return false;
		Boolean exists = cursor.getCount()>0;
		cursor.close();
		return exists;
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
				Book.AUTHOR+" TEXT COLLATE NOCASE,"+
				Book.AUTHOR2+" TEXT COLLATE NOCASE,"+
				Book.CATEGORY+" TEXT COLLATE NOCASE,"+
				Book.COMPLETED+" TEXT,"+
				Book.COPYRIGHTYEAR+" TEXT,"+
				Book.DESCRIPTION+" TEXT COLLATE NOCASE,"+
				Book.ETEXT+" TEXT,";
		
		for (int i=0; i<MAX_GENRES; i++)
			create += (DBGENRE_PREFIX+i)+" TEXT COLLATE NOCASE,";
		
		create +=
				Book.LANGUAGE+" TEXT COLLATE NOCASE,"+
				Book.RSSURL+" TEXT,"+
				Book.TITLE+" TEXT COLLATE NOCASE,"+
				Book.TOTALTIME+ " TEXT,"+
				Book.TRANSLATOR+ " TEXT COLLATE NOCASE,"+
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
	
	public static Cursor queryAuthors(SQLiteDatabase db, boolean onlyInstalled) {
		String query = "SELECT "+AUTHOR+" FROM "+BOOK_TABLE+ 
		   (onlyInstalled ? " WHERE "+ONLY_INSTALLED : "") +
		" UNION SELECT "+AUTHOR2+" FROM "+BOOK_TABLE+" WHERE "+ AUTHOR2 +"<>''"+
		   (onlyInstalled ? " AND "+ONLY_INSTALLED : "");
		Log.v("Book", query);
		return db.rawQuery(query, emptyStringArray);
	}
	
	public static String searchQuery(String searchText) {
		return AUTHOR + " || '' || " + AUTHOR2 + " || '' || " +
			TITLE + " || '' LIKE "+DatabaseUtils.sqlEscapeString("%"+searchText+"%") + " ";
	}
	
	public static String searchConjunct(String searchText) {
		if (searchText != null && searchText.length() > 0)
			return " AND "+searchQuery(searchText);
		else
			return "";
	}

	public static Cursor queryGenre(SQLiteDatabase db, String string, boolean onlyInstalled, String searchText) {
		String query = "SELECT "+QUERY_COLS+" FROM "+BOOK_TABLE+
		   " WHERE "+DatabaseUtils.sqlEscapeString(abbreviateGenre(string))+
		   " IN ("+getGenreColumns()+") "+
		   (onlyInstalled ? "AND "+ONLY_INSTALLED : "") +
		   searchConjunct(searchText) +
		   "ORDER BY "+AUTHOR+","+AUTHOR2+","+TITLE;
		Log.v("Book", query);
		return db.rawQuery(query, emptyStringArray);
	}

	public static Cursor queryAuthor(SQLiteDatabase db, String string, boolean onlyInstalled, String searchText) {
		String query = "SELECT "+QUERY_COLS+" FROM "+BOOK_TABLE+
		   " WHERE " + DatabaseUtils.sqlEscapeString(string)+
		   " IN ("+AUTHOR+","+AUTHOR2+") "+
		   (onlyInstalled ? "AND "+ONLY_INSTALLED+" " : "") +
		   searchConjunct(searchText) +
		   "ORDER BY "+TITLE;
		Log.v("Book", query);
		return db.rawQuery(query, emptyStringArray);
	}
	
	public static Cursor queryAll(SQLiteDatabase db, boolean onlyInstalled, String searchText) {
		String query = "SELECT "+QUERY_COLS+" FROM "+BOOK_TABLE;
		
		if (onlyInstalled && searchText != null) {
			query += " WHERE "+ONLY_INSTALLED+searchConjunct(searchText);
		}
		else if (onlyInstalled && searchText == null) {
			query += " WHERE "+ONLY_INSTALLED;
		}
		else if (!onlyInstalled && searchText != null) {
			query += " WHERE "+searchQuery(searchText);
		}
		
		query += " ORDER BY "+AUTHOR+","+AUTHOR2+","+TITLE;			

		Log.v("Book", query);
		return db.rawQuery(query, emptyStringArray);
	}
	
	public static SQLiteDatabase getDB(Context context) {
		return getDB(context, false);
	}

	public static SQLiteDatabase getDB(Context context, boolean create) throws SQLiteException {
		int attr = SQLiteDatabase.OPEN_READWRITE;
		if (create)
			attr |= SQLiteDatabase.CREATE_IF_NECESSARY;
		Log.v("Book", "opening "+getDBPath(context));
		return SQLiteDatabase.openDatabase(getDBPath(context), 
    			null, attr);
	}

	public String friendlyGenre(String string) {
		try {
			int i = Integer.parseInt(string);
			return standardGenres[i];
		}
		catch(NumberFormatException e) {
			return string;
		}
	}

	public String getInfo() {
		String authors = author;
		if (author2.length()>0) {
			authors += " &amp; "+author2;
		}
		String info = "<b>"+authors+"</b><br/><i>"+title+"</i><br/>";
		if (copyrightyear.length()>0) 
			info += copyrightyear+"<br/>";
		if (language.length()>0)
			info += language+"<br/>";
		if (translator.length()>0) 
			info += "Translated by "+translator+"<br/>";
		if (totaltime.length()>0)
			info += "Length: "+totaltime+"<br/>";
		for (int i=0; i<MAX_GENRES; i++) {
			if (genres[i].length()>0) {
				if (i!=0)
					info +=", ";
				info += friendlyGenre(genres[i]);				
			}
		}
		info += "<br/>";
		if (etext.length()>0)
			info += "<a href='"+etext+"'>"+etext+"</a><br/>";
		info += "<br/>";
		info += "<br/>"+description;
		return info;
	}

	public static String getDBPath(Context context) {
		return ExternalStorage.getSDCacheDir(context, "databases")+"/"+DB_FILENAME;
	}
}
