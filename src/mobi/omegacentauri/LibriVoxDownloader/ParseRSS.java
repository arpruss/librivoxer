package mobi.omegacentauri.LibriVoxDownloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import android.util.Xml;

public class ParseRSS extends DefaultHandler {
	private URL url;
	private ArrayList<URL> list;
	private String link;
	private StringBuilder builder;
	private int inItem;
	private int inChannel;
	
	public ParseRSS(URL url) {
		this.url = url;
		list = null;
	}

	public void parse() throws IOException, SAXException {
		list = new ArrayList<URL>();
		link = null;
		inItem = 0;
		inChannel = 0;

		Xml.parse(url.openStream(), Xml.Encoding.UTF_8, this);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		builder.append(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		super.endElement(uri, localName, name);
		if (localName.equalsIgnoreCase("item")) {
			inItem--;
		}
		else if (localName.equalsIgnoreCase("channel")) {
			inChannel--;
		}
		else if (localName.equalsIgnoreCase("link")) {
			if (inChannel == 1 && inItem == 0) {
				link = builder.toString().trim();
			}
			else if (inItem == 1) {
				try {
					list.add(new URL(builder.toString().trim()));
				} catch (MalformedURLException e) {
				}
			}
		}
		builder.setLength(0);
	}

	@Override
	public void startElement(String uri, String localName, String name, 
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		if (localName.equalsIgnoreCase("item")) {
			inItem++;
		}
		else if (localName.equalsIgnoreCase("channel")) {
			inChannel++;
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		builder = new StringBuilder();
	}
	
	
	public String getLink() {
		return link;
	}

	public List<URL> getList() {
		return list;
	}
}
