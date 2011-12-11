package mobi.omegacentauri.Librivoxer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ItemView extends Activity {
	SQLiteDatabase db;
	Map<String,String> data;
	SharedPreferences options;
	int id;

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
        data = Book.loadEntry(db, id); 
    	db.close();
    	
    	TextView info = (TextView)findViewById(R.id.info);
    	info.setText(Html.fromHtml(getInfo()));
	}
	
	private String getInfo() {
		String author = data.get(Book.AUTHOR);
		String author2 = data.get(Book.AUTHOR2);
		if (author2.length()>0) {
			author += " &amp; "+author2;
		}
		return "<b>"+author+"</b>, <i>"+data.get(Book.TITLE)+"</i><br/>" +
		data.get(Book.DESCRIPTION);
	}
        
	@Override
	public void onResume() {
		super.onResume();		
	}	


    private class DownloadTask extends AsyncTask<Void, Integer, String> {
    	ProgressDialog progress;
    	
    	public DownloadTask() {
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		progress = new ProgressDialog(ItemView.this);
    		progress.setCancelable(true);
    		progress.setMessage("Downloading "+data.get(Book.TITLE));
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
			try {
				URL url;
				if (options.getString(Options.PREF_FORMAT, Options.OPT_OGG).equals(Options.OPT_OGG)) {
					url = new URL(data.get(Book.RSSURL).replace("/rss/", "/rssogg/"));
				}
				else {
					url = new URL(data.get(Book.RSSURL));
				}
				ParseRSS parse = new ParseRSS(url);
				parse.parse();			
				List<URL> list = parse.getList();
				String b = parse.getLink().replaceAll("/$","").replaceAll(".*/","");
				String dir = getBookDir(b);
				
				publishProgress(1, list.size()+1);
				
				for (int i=0; i<list.size(); i++) {
					String filename = list.get(i).getPath().replaceAll(".*/", "");
					String path = dir+"/"+filename;
					download(list.get(i), path);
					did.add(path);
					publishProgress(2+i, list.size()+1);
				}

				return dir;
			}
			catch (IOException e) {
				for (int i=0; i<did.size(); i++) {
					(new File(did.get(i))).delete();
				}
				return null;
			}
		}

		private String getBookDir(String b) {
			String dir = Environment.getExternalStorageDirectory() + "/" + b;
			File dirF = new File(dir);
			dirF.mkdir();
			return dir;
		}
		
		private void download(URL url, String path) throws IOException {
			File tmpFile = null;
			InputStream in = null;
			OutputStream out = null;
			
			try {
				final int bufferSize = 16384;
				
				byte[] buffer = new byte[bufferSize];
							
				in = url.openStream();
				File outFile = new File(path);
				outFile.delete();
				tmpFile = File.createTempFile(path, "tmp");
				out = new FileOutputStream(tmpFile);
				
				int count;
				
				while ((count = in.read(buffer, 0, bufferSize)) >= 0) {
					if (isCancelled())
						throw new IOException(); // TODO: be nicer
					out.write(buffer, 0, count);
				}
				out.close();
				out = null;
				in.close();
				in = null;
				
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
			else {
				// TODO: mark in DB as downloaded
				// TODO: set up option to play
			}
		}
    }

}
