package mobi.omegacentauri.Librivoxer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ItemView extends Activity {
	SQLiteDatabase db;
	SharedPreferences options;
	Book book;
	int id;
	static public final String PARTIAL = "PARTIAL:";
	static public final String FULL = "FULL:";
	static public final String M3U = "book.m3u";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        options = PreferenceManager.getDefaultSharedPreferences(this);
        
        Log.v("Book", getIntent().toString());
        id = getIntent().getIntExtra(Book.DBID, -1);
        if (id < 0) {
        	finish();
        	return;
        }

        Log.v("Book", ""+id);
        
        setContentView(R.layout.item);
        
    	db = Book.getDB(this);
        book = new Book(db, id); 
    	db.close();
    	
    	TextView info = (TextView)findViewById(R.id.info);
    	info.setText(Html.fromHtml(book.getInfo()));
    	info.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		
		setButtons();
	}
	
	public void setButtons() {
		Button download;
		Button delete;
		Button play;
		
		download = (Button)findViewById(R.id.download);
		play = (Button)findViewById(R.id.play);
		delete = (Button)findViewById(R.id.delete);
		
		if (book.installed.startsWith(FULL)) {
			Log.v("Book", "full");
			download.setVisibility(View.INVISIBLE);
			play.setVisibility(View.VISIBLE);
			delete.setVisibility(View.VISIBLE);
		}
		else if (book.installed.startsWith(PARTIAL)) {
			Log.v("Book", "partial");
			download.setText("Continue download");
			download.setVisibility(View.VISIBLE);
			play.setVisibility(View.INVISIBLE);
			delete.setVisibility(View.VISIBLE);
		}
		else {
			Log.v("Book", "none");
			download.setText("Download");
			download.setVisibility(View.VISIBLE);
			play.setVisibility(View.INVISIBLE);
			delete.setVisibility(View.INVISIBLE);
		}		
	}
		
	
	public void setInstalled(String inst) {
    	db = Book.getDB(this);
    	String query = "UPDATE "+Book.BOOK_TABLE+" SET "+Book.INSTALLED+"="+
			DatabaseUtils.sqlEscapeString(inst)+" WHERE "+Book.DBID+"='"+id+"'";
    	Log.v("Book", query);
    	db.execSQL(query);
    	db.close();
		book.installed = inst;
		Log.v("Book", inst);
	}

	public void downloadClick(View v) {
		new DownloadTask().execute();
	}
	
	public void deleteClick(View v) {
		int index = book.installed.indexOf(":");
		if (index < 0)
			return;
		deleteDir(book.installed.substring(index+1));
		setInstalled("");
		setButtons();
	}
	
	public void playClick(View v) {
		String dir = book.installed.replaceAll("^[^:]+:", "");
		File f = new File(dir + "/" + M3U);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(f), "audio/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
//		Toast.makeText(ItemView.this, "Not yet implemented", 3000).show();
	}
        
    public class DownloadTask extends AsyncTask<Void, Integer, String> {
    	private ProgressDialog progress;
    	private static final String CANCEL = "//cancel//";
    	
    	public DownloadTask() {
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		progress = new ProgressDialog(ItemView.this);
    		progress.setCancelable(true);
			progress.setOnCancelListener(new OnCancelListener(){
				@Override
				public void onCancel(DialogInterface arg0) {
					DownloadTask.this.cancel(true);					
				}});
    		progress.setMessage("Downloading "+book.title);
    		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    		progress.setIndeterminate(true);
    		progress.show();
    	}

    	protected void onProgressUpdate(Integer... p) {
    		progress.setIndeterminate(false);
    		progress.setMax(p[1]);
    		progress.setProgress(p[0]);
    	}
    	
		@Override
		protected String doInBackground(Void... arg0) {
			ArrayList<String> did = new ArrayList<String>();
			String dir = null;
			try {
				boolean ogg = options.getString(Options.PREF_FORMAT, Options.OPT_OGG).equals(Options.OPT_OGG);
				boolean oggRSS = false;
				URL url;
				
				String rssURL = book.rssurl;
				if (rssURL.length() == 0) {
					rssURL = "http://librivox.org/rss/"+id;
				}
				
				if (ogg && rssURL.contains("/rss/")) {
					rssURL = rssURL.replace("/rss/", "/rssogg/");
					oggRSS = true;
				}

				Log.v("Book", rssURL);
				url = new URL(rssURL);
				ParseRSS parse = new ParseRSS(url);
				parse.parse();
				List<URL> list = parse.getList();
				
				if (ogg && (!oggRSS || list.size() == 0)) {
					list = getOGGList(new URL(parse.getLink()));
					if (list.size() == 0) {
						throw(new IOException("Cannot find ogg files"));
					}
				}
				
				String b = parse.getLink().replaceAll("/$","").replaceAll(".*/","");
				dir = getBookDir(b);
				
				cleanDir(dir);
				
				publishProgress(1, list.size()+1);
				
				for (int i=0; i<list.size(); i++) {
					String filename = list.get(i).getPath().replaceAll(".*/", "");
					String path = dir+"/"+filename;
					download(list.get(i), path);
					did.add(filename);
					publishProgress(2+i, list.size()+1);
				}
				
				File m3u = new File(dir+"/"+M3U);
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(m3u));
				for (String s: did) {
					writer.write(s);
					writer.write("\r\n");
				}
				writer.close();

				setInstalled(FULL+dir);
				return dir;
			}
			catch (SAXException e) {
				Log.v("Book", ""+e);
				return null;
			}
			catch (IOException e) {
				Log.v("Book", "<"+e.toString()+">");
				if (dir != null) {
					if (did.size()==0) {
						deleteDir(dir);
						setInstalled("");
					}
					else {
						setInstalled(PARTIAL+dir);
					}
				}
				
				if (e.toString().contains(CANCEL)) {
					Log.v("Book", "exception:cancel");
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							Log.v("Book", "onui");
							Toast.makeText(ItemView.this, "Canceled", 3000).show(); 
							setButtons();			
						}});
					return null;
				}
				else {
					return null;
				}
			}
		}

		private List<URL> getOGGList(URL url) throws IOException {
			List<URL> list = new ArrayList<URL>();
			String html = Utils.getStringFromURL(url);
			Pattern pattern = Pattern.compile("<a\\s+href=['\"]([^'\"]+\\.ogg)['\"]",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(html);
			int pos = 0;
			while (matcher.find(pos)) {
				Log.v("Book", "found:"+matcher.group(1));
				list.add(new URL(matcher.group(1)));
				pos = matcher.end();
			}
			return list;
		}
		
		private String getBookDir(String b) {
			String dir = Environment.getExternalStorageDirectory() + "/" + "Librivox";
			(new File(dir)).mkdir();
			dir += "/" + b;
			(new File(dir)).mkdir();
			Log.v("Book", "dir:"+dir);
			return dir;
		}
		
		private void download(URL url, String path) throws IOException {
			File tmpFile = null;
			InputStream in = null;
			OutputStream out = null;
			
			try {
				final int bufferSize = 16384;
				
				File outFile = new File(path);
				if (outFile.exists())
					return;

				byte[] buffer = new byte[bufferSize];
							
				in = url.openStream();
				tmpFile = new File(path + ".download");
				tmpFile.delete();
				out = new FileOutputStream(tmpFile);
				
				int count;
				
				while ((count = in.read(buffer, 0, bufferSize)) >= 0) {
					if (isCancelled()) {
						Log.v("Book", "throw");
						throw new IOException(CANCEL); // TODO: be nicer
					}
					out.write(buffer, 0, count);
				}
				out.close();
				out = null;
				in.close();
				in = null;

				Log.v("Book", "renaming");
				tmpFile.renameTo(outFile);
				tmpFile = null;
			}
			finally {
				if (in != null) in.close();
				if (out != null) out.close();
				if (tmpFile != null) tmpFile.delete();
			}
		}

		@Override
		protected void onPostExecute(String dir) {
			progress.dismiss();
			if (dir == null) {
				Toast.makeText(ItemView.this, "Error downloading", 3000).show();
			}
			setButtons();
		}
    }
    
    static private void deleteDir(String dir) {
    	File dirFile = new File(dir);
    	for (File f: dirFile.listFiles()) {
    		f.delete();
    	}
    	dirFile.delete();
    }

    static private void cleanDir(String dir) {
    	File dirFile = new File(dir);
    	for (File f: dirFile.listFiles()) {
    		if (f.getPath().endsWith(".download"))
    			f.delete();
    	}
    }
}
