package mobi.omegacentauri.Librivoxer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.util.Xml;

public class ParseToArray implements BookSaver {
	private InputStream stream;
	private ArrayList<Book> books;
	
	public ParseToArray(InputStream stream) {
		this.stream = stream;
		this.books = new ArrayList<Book>();
	}
	
	public ArrayList<Book> parse() {
		BookHandler handler = new BookHandler(this);
		try {
			Xml.parse(stream, Xml.Encoding.UTF_8, handler);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return books;
	}

	@Override
	public void saveStart() {
//		books = new ArrayList<Book>();		
	}

	@Override
	public void saveBook(Book book) {
		books.add(book);
	}

	@Override
	public void saveDone() {
	}
}
