package com.ra1ph.getpic.map;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Environment;
import com.ra1ph.getpic.ImageListAdapter;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;

import com.ra1ph.getpic.AsyncTask;
import com.ra1ph.getpic.MainActivity;
import com.ra1ph.getpic.image.EXIFProcessor;

public class MapTask extends AsyncTask<Void, Void, Void> {
	
	public static final String MAP_PATH="/maps/";
    private final int mapWidth;
    private final int mapHeight;

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
        SharedPreferences mPrefs = context.getSharedPreferences(MainActivity.PREFS_NAME, MainActivity.MODE_PRIVATE);
        mapWidth = mPrefs.getInt(ImageListAdapter.MAP_WIDTH, 0);
        mapHeight = mPrefs.getInt(ImageListAdapter.MAP_HEIGHT, 0);
    }
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
        request.xSize=(int)mapWidth;
        request.ySize=(int)mapHeight;
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
            Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
            drawPoint(mutableBitmap);
			BMPtoFile(mutableBitmap);
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

    public static Bitmap convertToMutable(Bitmap imgIn) {
        try {
            //this is the file going to use temporally to save the bytes.
            // This file will not be a image, it will store the raw image data.
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

            //Open an RandomAccessFile
            //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
            //into AndroidManifest.xml file
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

            // get the width and height of the source bitmap.
            int width = imgIn.getWidth();
            int height = imgIn.getHeight();
            Bitmap.Config type = imgIn.getConfig();

            //Copy the byte to the file
            //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
            FileChannel channel = randomAccessFile.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
            imgIn.copyPixelsToBuffer(map);
            //recycle the source bitmap, this will be no longer used.
            imgIn.recycle();
            System.gc();// try to force the bytes from the imgIn to be released

            //Create a new bitmap to load the bitmap again. Probably the memory will be available.
            imgIn = Bitmap.createBitmap(width, height, type);
            map.position(0);
            //load it back from temporary
            imgIn.copyPixelsFromBuffer(map);
            //close the temporary file and channel , then delete that also
            channel.close();
            randomAccessFile.close();

            // delete the temp file
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgIn;
    }

    private void drawPoint(Bitmap bmp) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);

        // create canvas to draw on the bitmap
        Canvas canvas = new Canvas(bmp);
        canvas.drawCircle(bmp.getWidth()/2, bmp.getHeight()/2, 8, paint);
    }

    public String BMPtoFile(Bitmap bitmap) {
		File f = new File(context.getExternalCacheDir()+MAP_PATH, filename);
		File dir = new File(context.getExternalCacheDir(), MAP_PATH);
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
