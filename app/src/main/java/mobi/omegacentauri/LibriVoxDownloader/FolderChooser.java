package mobi.omegacentauri.LibriVoxDownloader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FolderChooser extends ListActivity {
    private SharedPreferences options;
    private File currentFolder;
	private File parentFolder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_folder);
        
        options = PreferenceManager.getDefaultSharedPreferences(this);
        
        currentFolder = new File(options.getString(Options.PREF_FOLDER,
        		Options.defaultFolder()));
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
				return f.isDirectory() && f.canRead();
			}
    	});
    }
    
    private void scanFolder() {
    	if (!currentFolder.canRead()) {
    		Toast.makeText(this, "Invalid folder", 2000).show();
    		currentFolder = new File(Environment.getExternalStorageDirectory() + "/" + "LibriVox");
    		currentFolder.mkdirs();
    	}
    	
    	File[] contents = getFolder();
    	 
    	final ArrayList<File> files = new ArrayList<File>();
    	for (File f: contents)
    		files.add(f);
    	String parentPath = currentFolder.getParent();
    	if (parentPath != null) {
    		parentFolder = new File(parentPath);
    		files.add(parentFolder);
    	}
    	else {
    		parentFolder = null;
    	}
    	
    	Comparator<File> comp = new Comparator<File>(){

			@Override
			public int compare(File f1, File f2) {
				if (f1.equals(f2))
					return 0;
				if (f1.equals(parentFolder))
					return -1;
				else if (f2.equals(parentFolder))
					return 1;
				else 
					return f1.getName().compareToIgnoreCase(f2.getName());
			}};
    	Collections.sort(files, comp);
    	    	
    	ListAdapter adapter = new ArrayAdapter<File>(this, 
    			android.R.layout.simple_list_item_1,
    			files) {
    		@Override
    		public View getView(int position, View convertView, ViewGroup parent) {
    			View v;				

    			if (convertView == null) {
    				v = View.inflate(FolderChooser.this, R.layout.oneline, null);
    			}
    			else {
    				v = convertView;
    			}

    			if (files.get(position).equals(parentFolder)) {    				
    				((TextView)v.findViewById(R.id.text1)) 
    				.setText(Html.fromHtml("<em>[up]</em>"));
    			}
    			else {
    				((TextView)v.findViewById(R.id.text1))
    				.setText(files.get(position).getName());
    			}
    			return v;
    		}
    	};
    	
    	setListAdapter(adapter);
    	
		((TextView)findViewById(R.id.current_folder)).setText( 
				currentFolder.getPath());
    }
    
    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
    	currentFolder = (File) lv.getAdapter().getItem(position);
    	scanFolder();    	
    }
    
    public void chooseClick(View v) {
    	SharedPreferences.Editor ed = options.edit();
    	ed.putString(Options.PREF_FOLDER, currentFolder.getPath());
    	ed.commit();
    	finish();
    }
    
    public void defaultClick(View v) {
    	currentFolder = new File(Options.defaultFolder());
    	currentFolder.mkdirs();
    	SharedPreferences.Editor ed = options.edit();
    	ed.putString(Options.PREF_FOLDER, currentFolder.getPath());
    	ed.commit();
    	scanFolder();
    }
    
    public void createClick(View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        
        alertDialog.setTitle("Create new folder");
        alertDialog.setMessage("Name of new folder:");
        final EditText input = new EditText(this);
        alertDialog.setView(input);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
        		"OK", 
        		new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		if(!createFolder(input.getText().toString().trim())) {
        			Toast.makeText(FolderChooser.this, "Invalid folder name", 2000).show();
        		}
        	}
        });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
        	public void onCancel(DialogInterface dialog) {} });
        alertDialog.show();		
    }
    
    private boolean createFolder(String s) {
    	String p;
    	p = currentFolder.getPath();
    	File d;
    	if (p.endsWith("/"))
    		d = new File(p+s);
    	else
    		d = new File(p+"/"+s);
    	if (! d.mkdir()) {
    		return false;
    	}
    	currentFolder = d;
    	scanFolder();
    	return true;    	
    }
}
