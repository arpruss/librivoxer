package mobi.omegacentauri.Librivoxer;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

public class ItemView extends Activity {
	SQLiteDatabase db;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
        
	@Override
	public void onResume() {
		super.onResume();
		
    	db = Book.getDB(this); 
	}	
}
