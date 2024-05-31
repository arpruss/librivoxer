package mobi.omegacentauri.LibriVoxDownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.documentfile.provider.DocumentFile;

public class ItemView extends Activity {
	SQLiteDatabase db;

	SharedPreferences options;
	Book book;
	int id;
	boolean active;
	static public final String PARTIAL = "PARTIAL:";
	static public final String FULL = "FULL:";
	DownloadTask downloadTask;
	DecimalFormat format = new DecimalFormat("0.000");
	private static boolean useDocumentFile = Build.VERSION.SDK_INT < 19;

	protected void onActivityResult(int requestCode, int resultCode,
									Intent data) {
		FolderChooser.result(this, requestCode,resultCode,data);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        downloadTask = null;
        active = true;
        
        options = PreferenceManager.getDefaultSharedPreferences(this);
        
        id = options.getInt(Options.PREF_ID, -1);
        if (id < 0) {
        	finish();
        	return;
        }

        Log.v("Librivoxer", ""+id);
        
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
		
		active = true;
		
		setButtons();
	}
	
	@Override 
	public void onPause() {
		super.onPause();
		
		active = false;
	}
	
	@Override 
	public void onStop() {
		super.onStop();
		
		if (downloadTask != null) {
			downloadTask.cancel(true);
		}
	}
	
	public void setButtons() {
		if(active) {
			Button download;
			Button delete;
			Button play;
			
			download = (Button)findViewById(R.id.download);
			play = (Button)findViewById(R.id.play);
			delete = (Button)findViewById(R.id.delete);
			
			if (book.installed.startsWith(FULL)) {
				Log.v("Librivoxer", "full");
				download.setVisibility(View.INVISIBLE);
				play.setVisibility(View.INVISIBLE);
				delete.setVisibility(View.VISIBLE);
			}
			else if (book.installed.startsWith(PARTIAL)) {
				Log.v("Librivoxer", "partial");
				download.setText("Continue download");
				download.setVisibility(View.VISIBLE);
				play.setVisibility(View.INVISIBLE);
				delete.setVisibility(View.VISIBLE);
			}
			else {
				Log.v("Librivoxer", "none");
				download.setText("Download");
				download.setVisibility(View.VISIBLE);
				play.setVisibility(View.INVISIBLE);
				delete.setVisibility(View.INVISIBLE);
			}
		}
	}
		
	
	public void setInstalled(String inst) {
    	db = Book.getDB(this);
    	String query = "UPDATE "+Book.BOOK_TABLE+" SET "+Book.INSTALLED+"="+
			DatabaseUtils.sqlEscapeString(inst)+" WHERE "+Book.DBID+"='"+id+"'";
    	Log.v("Librivoxer", query);
    	db.execSQL(query);
    	db.close();
		book.installed = inst;
		Log.v("Librivoxer", inst);
	}

	public void downloadClick(View v) {
		new DownloadTask().execute();
	}
	
	public void deleteClick(View v) {
		int index = book.installed.indexOf(":");
		if (index < 0)
			return;
		deleteDir(this, book.installed.substring(index+1));
		setInstalled("");
		setButtons();
	}
	
	public void playClick(View v) {
		Intent intent = new Intent();

		if (Options.getString(options, Options.PREF_PLAY).equals(Options.OPT_PLAYLIST)) {
			String uri = book.installed.replaceAll("^[^:]+:", "");
			DocumentFile df = DocumentFile.fromTreeUri(this, Uri.parse(uri));
			DocumentFile found = null;
			if (df.exists()) {
				for (DocumentFile f : df.listFiles()) {
					if (f.getName().endsWith(".m3u")) {
						found = f;
						break;
					}
				}
			}
			if (found == null) {
				Toast.makeText(ItemView.this, "Cannot find m3u file", Toast.LENGTH_LONG).show();
			}
			else {
				Log.v("Librivoxer", "play "+found.getUri());
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(found.getUri(), "audio/*");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
//			File f = new File(dir + "/" + dir.replaceAll(".*/", "") + ".m3u");
//			Log.v("Librivoxer", "Play "+f.getPath());
//			intent.setAction(Intent.ACTION_VIEW);
//			intent.setDataAndType(Uri.fromFile(f), "audio/*");
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		else {
			intent.setAction("android.intent.action.MUSIC_PLAYER");
		}

		try {
			startActivity(intent);
		}  
		catch (ActivityNotFoundException e) {
			Toast.makeText(ItemView.this, "No audio player (get MortPlayer)", 6000).show(); 
		}
	}
        
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.update).setVisible(false);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.please_buy:
    		new PleaseBuy(this, true);
    		return true;
    	case R.id.license:
    		Browser.license(this);
    		return true;
    	case R.id.options:
			startActivity(new Intent(this, Options.class));			
			return true;
		case R.id.folder:
			FolderChooser.choose(this);
			return true;
    	}
    	return false;
    	
    }
	
    public class DownloadTask extends AsyncTask<Void, Integer, DocumentFile> {
    	private ProgressDialog progress;
    	private static final String CANCEL = "//cancel//";
    	private static final int INFO = 0;
    	private static final int AUDIO = 1;
    	private static final int COVER = 2;
    	private static final int FINALIZING = 3;
    	private static final int BYTE_COUNT = 4;
    	
    	public DownloadTask() {
    		downloadTask = this;
    	}
    	
    	@Override
    	protected void onCancelled() {
    		downloadTask = null;
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
    		switch(p[0]) {
    		case INFO:
    			progress.setIndeterminate(true);
    			progress.setMessage("Fetching file information...");
    			break;
    		case COVER:
    			progress.setIndeterminate(true);
    			progress.setMessage("Fetching cover...");
    			break;
    		case AUDIO:
    			progress.setIndeterminate(false);
        		progress.setMax(p[2]);
        		progress.setProgress(p[1]);
        		progress.setMessage("Fetching audio");
        		break;
    		case FINALIZING:
    			progress.setIndeterminate(true);
    			progress.setMessage("Finalizing...");
    			break;
    		case BYTE_COUNT:
    			
    			progress.setMessage(format.format(p[1]/(1024.*1024.))+"mb in chapter");
    			break;
    			
    		}
    	}
    	
		@Override
		protected DocumentFile doInBackground(Void... arg0) {
			ArrayList<String> did = new ArrayList<String>();
			DocumentFile dir = null;
			try {
//				boolean ogg = options.getString(Options.PREF_FORMAT, Options.OPT_OGG).equals(Options.OPT_OGG);
				boolean ogg = false; // can't find ogg files!
				boolean oggRSS = false;
				URL url;
				
				publishProgress(INFO);
				
//				String rssURL = book.rssurl;
//				if (rssURL.length() == 0) {
//					rssURL = "http://librivox.org/rss/"+id;
//				}

				String rssURL = "https://librivox.org/rss/"+id;
				
				if (ogg && rssURL.contains("/rss/")) {
					rssURL = rssURL.replace("/rss/", "/rssogg/");
					oggRSS = true;
				}

				Log.v("Librivoxer", rssURL);
				url = new URL(rssURL);
				ParseRSS parse = new ParseRSS(url);
				parse.parse();
				List<URL> list = parse.getList();
				Log.v("Librivoxer", "have list");
				MoreBookData data = new MoreBookData();
				Log.v("Librivoxer", "have more data");
				boolean needOGG = ogg && (!oggRSS || list.size() == 0);
				data.get(new URL(parse.getLink()), needOGG);
				if (needOGG)
					list = data.urls;

				String base = parse.getLink().replaceAll("/$","").replaceAll(".*/","");
				dir = getBookDir(base);
				if (dir == null)
					return null; // TODO

				Log.v("Librivoxer", "downloading to "+dir.getName());

				cleanDir(dir);
				
				publishProgress(AUDIO, 0, list.size());

				Log.v("Librivoxer", "downloading");
				
				for (int i=0; i<list.size(); i++) {
					Log.v("Librivoxer", "downloading "+i);
					String filename = list.get(i).getPath().replaceAll(".*/", "");
					if (null == dir.findFile(filename)) {
						Log.v("Librivoxer", "creating "+filename);
						DocumentFile f = dir.createFile("audio/mpeg", filename + ".download");
						Log.v("Librivoxer", "downloading "+f.getUri());
						if (download(list.get(i), f, true)) {
							f.renameTo(filename);
						} else {
							f.delete();
						}
						did.add(filename);
					}
					publishProgress(AUDIO, 1+i, list.size());
				}
				
				if (data.coverJPEG != null) {
					publishProgress(COVER);
					DocumentFile f = dir.createFile("image/jpeg", "cover.jpg");
					download(data.coverJPEG, f, false);
				}
				
				publishProgress(FINALIZING);

				DocumentFile m3u = dir.createFile("audio/x-mpegurl",base+".m3u");
				OutputStream out = getContentResolver().openOutputStream(m3u.getUri());
				OutputStreamWriter writer = new OutputStreamWriter(out);
				for (String s: did) {
					writer.write(s);
					writer.write("\r\n");
				}
				writer.close();

				setInstalled(FULL+dir.getUri());
				return dir;
			}
			catch (SAXException e) {
				Log.v("Librivoxer", ""+e);
				return null;
			}
			catch (IOException e) {
				Log.v("Librivoxer", "<"+e.toString()+">");
				if (dir != null) {
					if (did.size()==0) {
						dir.delete();
						setInstalled("");
					}
					else {
						setInstalled(PARTIAL+dir.getUri());
					}
				}
				
				if (e.toString().contains(CANCEL)) {
					Log.v("Librivoxer", "exception:cancel");
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
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
		
		public class MoreBookData {
			List<URL> urls;
			URL	coverJPEG;
			
			public MoreBookData() {
				coverJPEG = null;
				urls = new ArrayList<URL>();
			}
			
			public void get(URL url, boolean ogg) throws IOException {
				Log.v("Librivoxer", "MoreBookData:"+url);
				String html = Utils.getStringFromURL(url);
				if (ogg) {
					Pattern pattern = Pattern.compile("<a\\s+href=['\"]([^'\"]+\\.ogg)['\"]",
							Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
					Matcher matcher = pattern.matcher(html);
					int pos = 0;
					while (matcher.find(pos)) {
						urls.add(new URL(matcher.group(1)));
						pos = matcher.end();
					}
				}
				
				Pattern pattern = Pattern.compile("<a\\s+href=['\"]([^'\"]+CdCover[^'\"]+\\.jpg)['\"]",
						Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
				Matcher matcher = pattern.matcher(html);
				if (matcher.find()) {
					coverJPEG = new URL(matcher.group(1));
				}
				else {
					coverJPEG = null;
				}				
			}
		}

		private DocumentFile getBookDir(String b) {
			String dir = options.getString(Options.PREF_FOLDER_URI,
					"");
			if (dir.length() <= 0)
				return null; // TODO
			Log.v("LibriVoxer", "folder: "+dir);
			return DocumentFile.fromTreeUri(ItemView.this, Uri.parse(dir)).createDirectory(b);
		}
		
		private boolean download(URL url, DocumentFile path, boolean byteCount) throws IOException {
			File tmpFile = null;
			InputStream in = null;
			OutputStream out = null;
			boolean success = false;

			Log.v("Librivoxer", ""+url+" -> "+path);
			
			try {
				final int bufferSize = 16384;
				
				byte[] buffer = new byte[bufferSize];
				
				int tryCount = 0;

				while (!success) {
					try {
						in = TrustAll.openStream(url);
						out = getContentResolver().openOutputStream(path.getUri());

						int size = 0;
						int count;
						
						while ((count = in.read(buffer, 0, bufferSize)) >= 0) {							
							size += count;
							
							if (isCancelled()) {
								Log.v("Librivoxer", "throw");
								throw new IOException(CANCEL); // TODO: be nicer
							}
							out.write(buffer, 0, count);

							if (byteCount)
								publishProgress(BYTE_COUNT, size);
						}
						
						Log.v("Librivoxer", "download size: "+size);
						if (size == 0) {
							throw new IOException("Zero length file");
						}
						success = true;
					}
					catch(IOException e) {
						tryCount++;
						if (Integer.parseInt(options.getString(Options.PREF_RETRIES, "2")) < tryCount ||
								e.toString().contains(CANCEL)) {
							throw(e);
						}
						else {
							Log.e("Librivoxer", "Error "+e+", retrying");
							if (out != null) {
								out.close();
								out = null;
							}
							if (in != null) {
								in.close();
								in = null;
							}
						}
					}
				}
				
				out.close();
				out = null;
				in.close();
				in = null;
			}
			finally {
				if (in != null) in.close();
				if (out != null) out.close();
				if (tmpFile != null) tmpFile.delete();
			}
			return success;
		}

		@Override
		protected void onPostExecute(DocumentFile dir) {
			progress.dismiss();
			if (dir == null) {
				Toast.makeText(ItemView.this, "Error downloading, try later", 6000).show();
			}
			setButtons();
			downloadTask = null;
		}
    }
    
    static private void deleteDir(Context c, String dir) {
		Log.v("Librivoxer", "request delete "+dir);

		Uri uri = Uri.parse(dir);
		DocumentFile dirFile = DocumentFile.fromTreeUri(c, uri);

		if (!dirFile.exists())
    		return;
    	
    	DocumentFile[] list = dirFile.listFiles();
    	if (list != null)
	    	for (DocumentFile f: list)
	    		f.delete();
    	dirFile.delete();
    }

    static private void cleanDir(DocumentFile dir) {
		if (dir.exists()) {
			for (DocumentFile f : dir.listFiles()) {
				if (f.getName().endsWith(".download") || f.getName().contains(".mp3.download"))
					f.delete();
			}
		}
    }
}
