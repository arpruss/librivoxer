package mobi.omegacentauri.LibriVoxDownloader;

import java.util.ArrayList;

public class Authors {
	ArrayList<Author> authors;
	
	public static final String AUTHOR = "author";
	
	public Authors() {
		authors = new ArrayList<Author>();
	}

	public void add(Author author) {
		authors.add(author);
	}
	
	public int getCount() {
		return authors.size();
	}

	public Author get(int i) {
		return authors.get(i);
	}
}
