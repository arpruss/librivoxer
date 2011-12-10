package mobi.omegacentauri.Librivoxer;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class BookHandler extends DefaultHandler {
	private Book curBook;
	private StringBuilder builder;
	private BookSaver saver;
	
	public BookHandler(BookSaver saver) {
		this.saver = saver;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		builder.append(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		super.endElement(uri, localName, name);
		if (curBook != null) {
			if (localName.equalsIgnoreCase(Book.AUTHOR)) {
				curBook.author = getText();
			}
			else if (localName.equalsIgnoreCase(Book.AUTHOR2)) {
				curBook.author2 = getText();
			}
			else if (localName.equalsIgnoreCase(Book.CATEGORY)) {
				curBook.category = getText();
			}
			else if (localName.equalsIgnoreCase(Book.COMPLETED)) {
				curBook.completed = getText();
			}
			else if (localName.equalsIgnoreCase(Book.COPYRIGHTYEAR)) {
				curBook.copyrightyear = getText();
			}
			else if (localName.equalsIgnoreCase(Book.DESCRIPTION)) {
				curBook.description = getText();
			}
			else if (localName.equalsIgnoreCase(Book.ETEXT)) {
				curBook.etext = getText();
			}
			else if (localName.equalsIgnoreCase(Book.GENRE)) {
				curBook.genre = getText();
			}
			else if (localName.equalsIgnoreCase(Book.LANGUAGE)) {
				curBook.language = getText();
			}
			else if (localName.equalsIgnoreCase(Book.RSSURL)) {
				curBook.rssurl = getText();
			}
			else if (localName.equalsIgnoreCase(Book.TITLE)) {
				curBook.title = getText();
			}
			else if (localName.equalsIgnoreCase(Book.TOTALTIME)) {
				curBook.totaltime = getText();
			}
			else if (localName.equalsIgnoreCase(Book.TRANSLATOR)) {
				curBook.translator = getText();
			}
			else if (localName.equalsIgnoreCase("book")) {
				if (0 <= curBook.id) {
					saver.saveBook(curBook);
				}
			}
			builder.setLength(0);
		}
	}
	
	private String getText() {
		return builder.toString().trim();
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		builder = new StringBuilder();
		saver.saveStart();
	}
	
	@Override
	public void startElement(String uri, String localName, String name, 
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if (localName.equalsIgnoreCase("book")) {
			curBook = new Book();
			String value = attributes.getValue(Book.ID);
			if (value != null) {
				try {
					curBook.id = Integer.parseInt(value);
				}
				catch (NumberFormatException e) {
					curBook.id = -1;
				}
			}
		}
	}
	
	@Override
	public void endDocument() {
		saver.saveDone();
	}	
}
