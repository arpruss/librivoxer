package mobi.omegacentauri.Librivoxer;

import java.io.InputStream;
import java.util.ArrayList;

public class XMLCatalog {
	InputStream stream;
	
	public XMLCatalog(InputStream stream) {
		this.stream = stream;
	}
	
	public ArrayList<Book> parse() {
		ArrayList<Book> books = new ArrayList<Book>();
		
		return null;		
	}
}
