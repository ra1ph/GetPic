package com.ra1ph.getpic.map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

import com.ra1ph.getpic.AsyncTask;
import com.ra1ph.getpic.MainActivity;
import com.ra1ph.getpic.image.EXIFProcessor;

public class MapTask extends AsyncTask<Void, Void, Void> {
	
	public static final String MAP_PATH="/maps/";
	
	String URL = "http://maps.google.com/maps/api/staticmap?";
	Bitmap bmp = null;
	MapRequest request;
	String filename;
	Context context;

	public static class MapRequest {
		public Double latitude, longitude;
		public int zoom, xSize, ySize;
	}

	public MapTask(Context context, MapRequest request, String filename) {
		// TODO Auto-generated constructor stub
		this.request = request;
		this.filename = filename;
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		URL += "center=" + Double.toString(request.latitude) + ","
				+ Double.toString(request.longitude) + "&zoom="
				+ Integer.toString(request.zoom) + "&size="
				+ Integer.toString(request.xSize) + "x"
				+ Integer.toString(request.ySize) + "&sensor=true";
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(URL);

		InputStream in = null;
		try {
			in = httpclient.execute(request).getEntity().getContent();
			bmp = BitmapFactory.decodeStream(in);
			BMPtoFile(bmp);
			in.close();
			sendBroadcast(MainActivity.UPDATE_ALL);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}

	public String BMPtoFile(Bitmap bitmap) {
		File f = new File(context.getCacheDir()+MAP_PATH, filename);
		File dir = new File(context.getCacheDir(), MAP_PATH);
		try {
			
			dir.mkdirs();
			dir.createNewFile();
			f.createNewFile();

			// Convert bitmap to byte array
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.JPEG, 100, bos);
			byte[] bitmapdata = bos.toByteArray();

			// write the bytes in file
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(bitmapdata);

			return f.getName();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	private void sendBroadcast(int ACTION) {
		Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
		intent.putExtra(MainActivity.KEY_ACTION, ACTION);
		context.sendBroadcast(intent);
	}
}
