package com.ra1ph.getpic;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.image.EXIFProcessor;
import com.ra1ph.getpic.image.ImageAsync;
import com.ra1ph.getpic.map.MapTask;
import com.ra1ph.getpic.message.Message;
import com.ra1ph.getpic.users.User;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

public class ImageListAdapter extends BaseAdapter {
    private final ImageLoader imageLoader;
    public ArrayList<User> items;
	Activity context;
	Bitmap cap;
    private int mapWidth,mapHeight;

    public static final String MAP_WIDTH = "map_width";
    public static final String MAP_HEIGHT = "map_height";
	
	static class ViewHolder {
        protected ImageView image;
        protected RelativeLayout layout;
    }

	public ImageListAdapter(Activity context, ArrayList<User> items) {
		this.context=context;
		this.items = items;
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(context);
        imageLoader.init(config);
		cap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cap);
        SharedPreferences mPrefs = context.getSharedPreferences(MainActivity.PREFS_NAME, MainActivity.MODE_PRIVATE);
        mapWidth = mPrefs.getInt(MAP_WIDTH,0);
        mapHeight = mPrefs.getInt(MAP_HEIGHT,0);
        if((mapWidth==0)||(mapHeight==0)){
            Resources r = context.getResources();
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            size.x = display.getWidth();
            size.y = display.getHeight();

            mapHeight = r.getDimensionPixelSize(R.dimen.map_heigth);
            mapWidth = size.x - (r.getDimensionPixelSize(R.dimen.listview_margin)*4);
            mPrefs.edit()
                    .putInt(MAP_HEIGHT,(int)mapHeight)
                    .putInt(MAP_WIDTH,(int)mapWidth)
                    .commit();
        }
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return items.size();
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		if (convertView == null) {
            LayoutInflater inflator = ((Activity)context).getLayoutInflater();
            view = inflator.inflate(R.layout.listview_item, null);
            
        } else {
            view = convertView;
        }
		ImageView image = (ImageView) view.findViewById(R.id.image_listview);
		ImageView delete = (ImageView) view.findViewById(R.id.user_delete);
		ImageView spam = (ImageView) view.findViewById(R.id.mark_spam);
		ImageView message = (ImageView) view.findViewById(R.id.send_message);
		ImageView map = (ImageView) view.findViewById(R.id.map_point);
		ImageView mapView = (ImageView) view.findViewById(R.id.image_map);
		final RelativeLayout mapLay = (RelativeLayout) view.findViewById(R.id.map_lay);
		final RelativeLayout bar = (RelativeLayout) view.findViewById(R.id.layout_bar);
		
		String path = "file://"+context.getExternalCacheDir().getPath()+"/"+items.get(position).picture;
		String pathMap = "file://"+context.getExternalCacheDir().getPath()+MapTask.MAP_PATH+items.get(position).picture;
		
		/*image.setImageBitmap(cap);
		ImageAsync loader = new ImageAsync(image,context,mapWidth,mapHeight);
		loader.execute(path);
		
		ImageAsync loaderMap = new ImageAsync(mapView,context,mapWidth,mapHeight);
		loaderMap.execute(pathMap);            */

        DisplayImageOptions options1 = new     DisplayImageOptions.Builder().showStubImage(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.ic_launcher).cacheInMemory().cacheOnDisc().build();
        imageLoader.displayImage(path,image,options1);
        imageLoader.displayImage(pathMap,mapView,options1);

		image.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(bar.getVisibility()==View.INVISIBLE) bar.setVisibility(View.VISIBLE);
				else bar.setVisibility(View.INVISIBLE);
			}
		});

        message.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //To change body of implemented methods use File | Settings | File Templates.
                Intent i = new Intent(context,ChatActivity.class);
                i.putExtra(User.USER_ID,items.get(position).user_id);
                context.startActivity(i);
            }
        });
		
		delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DBHelper helper = DBHelper.getInstance(context);
				User.deleteUser(items.get(position).user_id, helper);
				Message.deleteUser(items.get(position).user_id, helper);
			}
		});
		
		map.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			
			mapLay.setVisibility(View.VISIBLE);
			}
		});
		
		mapLay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			v.setVisibility(View.INVISIBLE);	
			}
		});
		return view;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}


}
