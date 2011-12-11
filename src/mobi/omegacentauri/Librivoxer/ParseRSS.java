package mobi.omegacentauri.Librivoxer;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.helpers.DefaultHandler;

public class ParseRSS extends DefaultHandler {
	private URL url;
	private ArrayList<URL> list;
	private String link;
	private StringBuilder builder;
	
	public ParseRSS(URL urL) {
		this.url = url;
		list = new ArrayList<URL>();
		link = null;
	}

	public void parse() {
		
	}

	public List<URL> getList() {
		return null;
	}

	public String getLink() {
		return link;
	}
}
