package mobi.omegacentauri.Librivoxer;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
				curBook.author = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.AUTHOR2)) {
				curBook.author2 = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.CATEGORY)) {
				curBook.category = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.COMPLETED)) {
				curBook.completed = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.COPYRIGHTYEAR)) {
				curBook.copyrightyear = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.DESCRIPTION)) {
				curBook.description = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.ETEXT)) {
				curBook.etext = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.GENRE)) {
				curBook.genre = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.LANGUAGE)) {
				curBook.language = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.RSSURL)) {
				curBook.rssurl = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.TITLE)) {
				curBook.title = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.TOTALTIME)) {
				curBook.totaltime = builder.toString();
			}
			else if (localName.equalsIgnoreCase(Book.TRANSLATOR)) {
				curBook.translator = builder.toString();
			}
			else if (localName.equalsIgnoreCase("book")) {
				if (0 <= curBook.id) {
					saver.saveBook(curBook);
				}
			}
			builder.setLength(0);
		}
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
			String value = attributes.getValue("id");
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
