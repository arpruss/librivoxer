package mobi.omegacentauri.Librivoxer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.util.Xml;

public class ParseToList implements BookSaver {
	private InputStream stream;
	private List<Book> books;
	
	public ParseToList(InputStream stream, List<Book> books) {
		this.stream = stream;
		this.books = books;
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
		books.add(book);
	}

	public void saveDone() {
	}
}
