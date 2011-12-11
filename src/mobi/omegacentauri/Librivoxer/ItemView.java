package mobi.omegacentauri.Librivoxer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
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
    	info.setMovementMethod(new ScrollingMovementMethod());
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
	
	public void downloadClick(View v) {
		new DownloadTask().execute();
	}
        
	@Override
	public void onResume() {
		super.onResume();		
	}	


    private class DownloadTask extends AsyncTask<Void, Integer, String> {
    	private static final String ZIP_TEMP = "mp3files.zip";
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
    		if (p[2] == 1) {
    			progress.setMessage("Unzipping...");
    		}
    		if (p[1] < 0) {
    			progress.setIndeterminate(true);
    		}
    		else {
    			progress.setIndeterminate(false);
        		progress.setMax(p[1]);
        		progress.setProgress(p[0]);
    		}
    	}
    	
		@Override
		protected String doInBackground(Void... arg0) {
			ArrayList<String> did = new ArrayList<String>();
			try {
				URL url;
				
				url = new URL(data.get(Book.ZIPFILE));

				String dir = getBookDir(data.get(Book.ZIPFILE));

				Log.v("Book", "Downloading "+url+" to:"+dir);
				File zipFile = new File(dir+"/"+ZIP_TEMP);
				download(url, zipFile);
				unzip(dir, zipFile);
				zipFile.delete();
				
				return dir;
			}
			catch (IOException e) {
				Log.v("Book", "Error "+e);
				for (int i=0; i<did.size(); i++) {
					(new File(did.get(i))).delete();
				}
				return null;
			}
		}
		
		private String getBookDir(String zip) {
			File zipFile = new File(zip);
			
			String dir = Environment.getExternalStorageDirectory() + "/" + "Librivox";
			(new File(dir)).mkdir();
			
			dir += "/" + zipFile.getName().replaceAll(".zip$", ""); 
			
			File dirF = new File(dir);
			dirF.mkdir();
			return dir;
		}
		
		private void download(URL url, File outFile) throws IOException {
			InputStream in = null;
			OutputStream out = null;
			File tmpFile = null;
			URLConnection connection = null;
			
			try {
				final int bufferSize = 16384;
				
				Log.v("Book", "Starting download");
				outFile.delete();

				connection = url.openConnection();
				int length = connection.getContentLength();
				Log.v("Book", "length="+length);
				
				in = connection.getInputStream();
				Log.v("Book", "opened stream");
				tmpFile = new File(outFile.getPath()+".download");
				Log.v("Book", "tmp:"+tmpFile.getPath());
				out = new FileOutputStream(tmpFile);
				Log.v("Book", "opened");
				
				int did = 0;
				int count;
				
				byte[] buffer = new byte[bufferSize];
				
				while ((count = in.read(buffer, 0, bufferSize)) >= 0) {
					if (isCancelled())
						throw new IOException("canceled"); // TODO: be nicer
					out.write(buffer, 0, count);
					publishProgress(did, length, 0);
					did += count;
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
