package mobi.omegacentauri.LibriVoxDownloader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FolderChooser extends ListActivity {
    private SharedPreferences options;
    private File currentFolder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_folder);
        
        options = PreferenceManager.getDefaultSharedPreferences(this);
        
        currentFolder = new File(options.getString(Options.PREF_FOLDER,
        		Environment.getExternalStorageDirectory() + "/" + "LibriVox"));
        currentFolder.mkdirs();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	scanFolder();
    }
    
    private File[] getFolder() {
    	return currentFolder.listFiles(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() && f.canRead() &&
				    ! f.getName().equals(".");
			}
    	});
    }
    
    private void scanFolder() {
    	File[] contents = getFolder();
    	if (contents.length == 0) {
			Toast.makeText(this, "Invalid folder, switching", 2000).show();
			currentFolder = new File(Environment.getExternalStorageDirectory() + "/" + "LibriVox");
			currentFolder.mkdirs();
			contents = getFolder();
    	}
    	
    	final File[] files = contents;
    	
    	ListAdapter adapter = new ArrayAdapter<File>(this, 
    			android.R.layout.simple_list_item_1,
    			contents) {
    		@Override
    		public View getView(int position, View convertView, ViewGroup parent) {
    			View v;				

    			if (convertView == null) {
    				v = View.inflate(FolderChooser.this, R.layout.oneline, null);
    			}
    			else {
    				v = convertView;
    			}

    			if (files[position].getName().equals("..")) 
    				((TextView)v.findViewById(R.id.text1))
    				.setText(Html.fromHtml("<em>[up]</em>"));
    			else
    				((TextView)v.findViewById(R.id.text1))
    				.setText(files[position].getName());
    			return v;
    		}
    	};
    	
    	setListAdapter(adapter);
    	
    	try {
			((TextView)findViewById(R.id.current_folder)).setText( 
					currentFolder.getCanonicalPath());
		} catch (IOException e) {
			((TextView)findViewById(R.id.current_folder)).setText("");
		}
    }
    
    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
    	currentFolder = (File) lv.getAdapter().getItem(position);
    	scanFolder();    	
    }
    
    public void chooseClick(View v) {
    	
    }
    
    public void createClick(View v) {
    	
    }
}
