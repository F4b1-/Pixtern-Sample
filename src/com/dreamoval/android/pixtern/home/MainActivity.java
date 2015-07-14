package com.dreamoval.android.pixtern.home;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.dreamoval.android.pixtern.card.utils.DataHolder;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = ""+MainActivity.class.getName();
	final int  req = 888;
	final int  reqReal = 889;
	final int  reqRealFace = 999;
	private String theSelfie = "";
	private HashMap theUpload = new HashMap();
	private String detection = "";

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if(v.getId() == R.id.imageView2) {
			menu.setHeaderTitle("Validation Types");
			menu.add(0, v.getId(), 0, "FACE");
			SubMenu sub=menu.addSubMenu(0, v.getId(), 0, "CARD");
			sub.add(0, v.getId(), 0,  "-VOTERSID-");
			sub.add(0, v.getId(), 0,  "-PERSONALAUSWEIS-");
			sub.add(0, v.getId(), 0,  "-PASSPORT-");
			sub.add(0, v.getId(), 0,  "-DETECT-");
			detection = "upload";
		}

		else{
			menu.setHeaderTitle("Type of Picture");
			menu.add(0, v.getId(), 0, "FACE");
			SubMenu sub=menu.addSubMenu(0, v.getId(), 0, "CARD");
			sub.add(0, v.getId(), 0,  "-VOTERSID-");
			sub.add(0, v.getId(), 0,  "-PERSONALAUSWEIS-");
			sub.add(0, v.getId(), 0,  "-PASSPORT-");
			sub.add(0, v.getId(), 0,  "-DETECT-");
			detection = "realtime";
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle().equals("FACE") && detection.equals("upload")) {
			startActivityForResult(new Intent(MainActivity.this, com.dreamoval.android.pixtern.face.ValidateUploadActivity.class),req);
		} else if (detection.equals("upload") && !item.getTitle().equals("CARD")){
			DataHolder.getInstance().setData(item.getTitle().toString());
			startActivityForResult(new Intent(MainActivity.this, com.dreamoval.android.pixtern.card.CardValidationActivity.class),req);	
		}
		else if (item.getTitle().equals("FACE") && detection.equals("realtime")) {
			startActivityForResult(new Intent(MainActivity.this, com.dreamoval.android.pixtern.face.FdActivity.class),reqRealFace);
		} else if (detection.equals("realtime") && !item.getTitle().equals("CARD")){
			DataHolder.getInstance().setData(item.getTitle().toString());
			startActivityForResult(new Intent(MainActivity.this, com.dreamoval.android.pixtern.card.CardRealtimeActivity.class),reqReal);	
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		ImageView iV = (ImageView)findViewById(R.id.imageView2);
		iV.setOnClickListener(this);
		registerForContextMenu(iV);

		ImageView iV2 = (ImageView)findViewById(R.id.imageView22);
		iV2.setOnClickListener(this);
		registerForContextMenu(iV2);
	}

	public void onClick(View v) {
		this.openContextMenu(v);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		//super.onActivityResult(requestCode, resultCode, data);	
		if (data != null)
		{
			//IF returning from Validation
			if(requestCode == 888) {
				tellus(data.getStringExtra("theSelfie"));
				tellus(data.getSerializableExtra("theValidation").toString());
				theUpload = (HashMap) data.getSerializableExtra("theValidation");

				if(theUpload.containsKey("Card") && theUpload.get("Card").equals("-VOTERSID-")) {
					Iterator entries = theUpload.entrySet().iterator();
					while (entries.hasNext()) {
						Entry thisEntry = (Entry) entries.next();
						String key = (String)thisEntry.getKey();
						String value =(String) thisEntry.getValue();
						if(key.equals("Age")) thisEntry.setValue(value.replaceAll("[^0-9]", ""));
						if(key.equals("Gender")) {
							if(value.contains("Female")) value = "Female";
							else value = "Male";
							thisEntry.setValue(value);
						}
						if(key.equals("Regdate")) thisEntry.setValue(value.replaceAll("[^0-9\\/]", ""));
						if(key.equals("Name")) {
							value = value.substring(value.indexOf(" ") + 1);
							thisEntry.setValue(value.replaceAll("[^A-Z ]", ""));
						}
						if(key.equals("ID")) thisEntry.setValue(value.replaceAll("[^0-9]", ""));
					}
				}

				if(theUpload.containsKey("Card") && theUpload.get("Card").equals("-PASSPORT-")) {
					Iterator entries = theUpload.entrySet().iterator();
					while (entries.hasNext()) {
						Entry thisEntry = (Entry) entries.next();
						String key = (String)thisEntry.getKey();
						String value =(String) thisEntry.getValue();
						thisEntry.setValue(value.replaceAll("[\n]", ""));
						thisEntry.setValue(value.replaceAll("[^A-Z0-9 ]", ""));
						if(key.equals("Nationality")) {
							if(value.contains("GHA") || value.contains("AIAN")) thisEntry.setValue("GHANAIAN");
						}
						if(key.equals("Sex")) {
							if(value.contains("M")) thisEntry.setValue("M");
							else if(value.contains("F")) thisEntry.setValue("F");
							else thisEntry.setValue("");
						}
					}
				}
				
				if(theUpload.containsKey("Card") && theUpload.get("Card").equals("-PERSONALAUSWEIS-")) {
					Iterator entries = theUpload.entrySet().iterator();
					while (entries.hasNext()) {
						Entry thisEntry = (Entry) entries.next();
						String key = (String)thisEntry.getKey();
						String value =(String) thisEntry.getValue();
						if (value.indexOf('\n') != -1){
							value = value.substring(value.indexOf('\n', value.indexOf('\n')) + 1);
							value.replaceAll("[\n]", "");
							thisEntry.setValue(value);
						}
						if(key.equals("Birthdate") || key.equals("Expiry")) {
							thisEntry.setValue(value.replaceAll("[^0-9.]", ""));
						}
						
						if(key.equals("Nationality")) {
							if(value.contains("DE") || value.contains("TSCH")) thisEntry.setValue("DEUTSCH");
						}
					}
				}
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						EditText textView = (EditText) findViewById(R.id.editText1);
						textView.setText(theUpload.toString());
					}
				});
			} else if(requestCode == 999) {
				theSelfie = data.getStringExtra("theSelfie").toString();
				tellus(data.getStringExtra("theSelfie").toString());

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						EditText textView = (EditText) findViewById(R.id.editText1);
						textView.setText(theSelfie);
					}
				});
			}
			else {
				startActivityForResult(new Intent(MainActivity.this, com.dreamoval.android.pixtern.card.CardValidationActivity.class),req);	
			}
		}
		else {
			tellus("No data has been received");
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	private void tellus(String tell){
		Log.i(""+TAG, ""+tell);	
	}

}
