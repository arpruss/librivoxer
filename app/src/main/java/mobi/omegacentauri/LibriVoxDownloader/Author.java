package mobi.omegacentauri.LibriVoxDownloader;

public class Author {
	String firstName;
	String lastName;
	
	public static final String FIRST_NAME = "first_name"; 
	public static final String LAST_NAME = "last_name"; 

	public Author() {
		firstName = "";
		lastName = "";
	}
	
	public String getName() {
		return lastName+", "+firstName;
	}
} 

