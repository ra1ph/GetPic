package com.ra1ph.getpic.image;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.ra1ph.getpic.AsyncTask;

public class ImageAsync extends AsyncTask<String, Void, Void> {

	WeakReference<ImageView> imageRef;
	WeakReference<Activity> activity;
	
	public ImageAsync(ImageView image, Activity activity) {
		// TODO Auto-generated constructor stub
		imageRef=new WeakReference<ImageView>(image);
		this.activity = new WeakReference<Activity>(activity);
	}
	
	@Override
	protected Void doInBackground(String... params) {
		// TODO Auto-generated method stub
		final ImageView image = imageRef.get();
		final Bitmap img = BitmapFactory.decodeFile(params[0]);
		Runnable run = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				image.setImageBitmap(img);
			}
			
		};
		Activity a = activity.get();
		if((a!=null)&&(image!=null))a.runOnUiThread(run);
		return null;
	}

}
