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
    int width=0,height=0;
    private int maxWidth,maxHeight;

    public ImageAsync(ImageView image, Activity activity, int maxWidth, int maxHeight) {
		// TODO Auto-generated constructor stub
		imageRef=new WeakReference<ImageView>(image);
		this.activity = new WeakReference<Activity>(activity);
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
	}
	
	@Override
	protected Void doInBackground(String... params) {
		// TODO Auto-generated method stub
		final ImageView image = imageRef.get();
		Bitmap bitmap = BitmapFactory.decodeFile(params[0]);
        double xSc = (double)bitmap.getWidth()/(double)maxWidth;
        double ySc = (double)bitmap.getHeight()/(double)maxHeight;
        if(xSc>ySc){
            width = maxWidth;
            height = (int)(bitmap.getHeight()/xSc);
        }else if(ySc>xSc){
            height = maxHeight;
            width = (int)(bitmap.getWidth()/ySc);
        }else {
            height = maxHeight;
            width = maxWidth;
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, width,height,true);
        final WeakReference<Bitmap> img = new WeakReference<Bitmap>(bitmap);
		Runnable run = new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
                if(img.get()!=null)
				image.setImageBitmap(img.get());
			}
			
		};
		Activity a = activity.get();
		if((a!=null)&&(image!=null))a.runOnUiThread(run);
		return null;
	}

}
