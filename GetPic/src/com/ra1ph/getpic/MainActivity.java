package com.ra1ph.getpic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.ra1ph.getpic.service.XMPPService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final String PREFS_NAME="prefs";
	private static final int CAMERA_PIC_REQUEST = 2500;
	private static final String TEMP_FILENAME = "temp";
	private String send_user_id = null;
	private SharedPreferences mPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPrefs = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
		
		Button btn = (Button) findViewById(R.id.button);
		Intent i = new Intent(MainActivity.this, XMPPService.class);
		startService(i);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MainActivity.this, XMPPService.class);
				i.putExtra(XMPPService.CODE_ACTION, XMPPService.NEW_TEXT_MESSAGE);
				i.putExtra(XMPPService.MESSAGE_TO, "ra1ph@jabber.ru/Smack");
				i.putExtra(XMPPService.MESSAGE_BODY, "Hello!");		
				startService(i);
				openCamera("ra1ph@jabber.ru/Smack");
			}
		});
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
		bitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
		byte[] bitmapdata = bos.toByteArray();

		//write the bytes in file
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(bitmapdata);
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

}
