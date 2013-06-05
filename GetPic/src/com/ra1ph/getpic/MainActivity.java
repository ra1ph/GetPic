package com.ra1ph.getpic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import net.simonvt.menudrawer.MenuDrawer;

import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.database.DBHelper.LoadListener;
import com.ra1ph.getpic.image.EXIFProcessor;
import com.ra1ph.getpic.service.GPSTracker;
import com.ra1ph.getpic.service.XMPPService;
import com.ra1ph.getpic.users.User;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends SuperActivity implements LoadListener{

	public static final String PREFS_NAME="prefs";
	private static final int CAMERA_PIC_REQUEST = 2500;
	private static final String TEMP_FILENAME = "temp";
	private String send_user_id = null;
	private SharedPreferences mPrefs;
	private ListView imageList;
	DBHelper helper;
	ArrayList<User> items;
	ImageListAdapter adapter;
	BroadcastReceiver br;
	
	GPSTracker gps;
	
	public final static String BROADCAST_ACTION = "com.ra1ph.getpic.broadcastupdate";
	public final static String KEY_ACTION = "action";
	public final static int UPDATE_ALL=0x0010; 
	
	private static final String BOT_JID = "ra1ph@jabber.ru/Smack";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mMenuDrawer.setContentView(R.layout.activity_main);
		/*MenuFragment menu = (MenuFragment)getSupportFragmentManager().findFragmentById(R.id.f_menu);
		menu.getListView().setOnItemClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
		
		
		mPrefs = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
		imageList=(ListView) findViewById(R.id.image_list);
		helper = new DBHelper(this);
		SQLiteDatabase db = helper.getReadableDatabase();
		items = new ArrayList<User>();
		adapter = new ImageListAdapter(this,items);
		imageList.setAdapter(adapter);
		
		User.getUsers(this, helper);
		
		Intent i = new Intent(MainActivity.this, XMPPService.class);
		startService(i);
		
		ImageView shot = (ImageView) findViewById(R.id.shot_btn);
		shot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openCamera(BOT_JID);
			}
		});
		
		registerBroadcast();
		gps = new GPSTracker(this);
		if(!gps.canGetLocation())Log.d(Constants.DEBUG_TAG, "GPS IS NOT ENABLED!!!");
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterBroadcast();
		
		super.onDestroy();
	}
	
	private void unregisterBroadcast(){
		unregisterReceiver(br);
	}
	
	private void registerBroadcast(){
		br = new BroadcastReceiver() {
		      public void onReceive(Context context, Intent intent) {
		    	  int action = intent.getIntExtra(KEY_ACTION, -1);
		    	  switch(action){
		    	  case UPDATE_ALL:
		    		  User.getUsers(MainActivity.this, helper);
		    		  Log.d("myLog", "updated all");
		    		  break;
		    	  }
		      }
		    };
		    IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
		    registerReceiver(br, intFilt);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub		
		if ((requestCode == CAMERA_PIC_REQUEST)&&(resultCode==RESULT_OK)) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            String user_id = mPrefs.getString(XMPPService.MESSAGE_TO, null);
            String filename = BMPtoFile(image);            
            sendImage(user_id, filename);
      }
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public String BMPtoFile(Bitmap bitmap){
		File f = new File(this.getCacheDir(), TEMP_FILENAME);
		try {
			f.createNewFile();		

		//Convert bitmap to byte array
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG,  100, bos);
		byte[] bitmapdata = bos.toByteArray();

		//write the bytes in file
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(bitmapdata);
		
		EXIFProcessor exif = new EXIFProcessor(f);
		exif.UpdateGeoTag(gps.getLatitude(), gps.getLongitude());
		
		return f.getName();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	public void openCamera(String user_id){
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		mPrefs.edit().putString(XMPPService.MESSAGE_TO, user_id).commit();
		
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
	}
	
	public void sendImage(String user_id, String filename){
		Intent i = new Intent(MainActivity.this, XMPPService.class);
		i.putExtra(XMPPService.CODE_ACTION, XMPPService.NEW_IMAGE_MESSAGE);
		i.putExtra(XMPPService.MESSAGE_TO, user_id);
		i.putExtra(XMPPService.MESSAGE_BODY, filename);		
		startService(i);
	}
	
	public void stopService(){
		
	}

	@Override
	public void onLoadListener(Object object) {
		// TODO Auto-generated method stub
		items = (ArrayList<User>) object;
		adapter.items = items;
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		gps = new GPSTracker(this);
		if(!gps.canGetLocation())Log.d(Constants.DEBUG_TAG, "GPS IS NOT ENABLED!!!");
		super.onResume();
	}

}
