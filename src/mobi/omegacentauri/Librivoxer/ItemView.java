package mobi.omegacentauri.Librivoxer;

import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class ItemView extends Activity {
	SQLiteDatabase db;
	Map<String,String> data;
	int id;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
}
