package mobi.omegacentauri.Librivoxer;

public class Book {
	// These tags are both for xml and sqlite
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
	public int id;
	
	public Book() {
		id = -1;
	}
}
