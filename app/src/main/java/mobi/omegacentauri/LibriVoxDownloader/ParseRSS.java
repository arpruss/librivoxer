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
		
		Log.v("librivoxer", "parse");

		Xml.parse(TrustAll.openStream(url), Xml.Encoding.UTF_8, this);
		Log.v("librivoxer", "parsed");
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
			Log.v("librivoxer", "-item");
		}
		else if (localName.equalsIgnoreCase("channel")) {
			inChannel--;
			Log.v("librivoxer", "-channel");
		}
		else if (localName.equalsIgnoreCase("link")) {
			if (inChannel == 1 && inItem == 0) {
				String l = builder.toString().trim();
				if (l.length() > 0) {
					link = l;
				}
			}
			else if (inItem == 1) {
				Log.v("librivoxer", "link inItem");
				try {
					String l = builder.toString().trim(); 
					if (l.endsWith(".mp3") || l.endsWith(".ogg"))
						list.add(new URL(l));
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
			Log.v("librivoxer", "+item");
			inItem++;
		}
		else if (localName.equalsIgnoreCase("channel")) {
			Log.v("librivoxer", "+channel");
			inChannel++;
		}
		else if (localName.equalsIgnoreCase("enclosure") && inItem > 0) {
			String url = attributes.getValue("url");
			try {
				list.add(new URL(url.trim()));
			}
			catch(MalformedURLException e) {
			}
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		builder = new StringBuilder();
	}
	
	
	public String getLink() {
		Log.v("librivoxer", "rss link "+link);
		return link;
	}

	public List<URL> getList() {
		return list;
	}
}
