package com.ra1ph.getpic;

import java.util.ArrayList;

import android.content.Intent;
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
	public ArrayList<User> items;	
	Activity context;
	Bitmap cap;
	
	static class ViewHolder {
        protected ImageView image;
        protected RelativeLayout layout;
    }

	public ImageListAdapter(Activity context, ArrayList<User> items) {
		this.context=context;
		this.items = items;
		cap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cap);
		// TODO Auto-generated constructor stub
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
		
		String path = context.getCacheDir().getPath()+"/"+items.get(position).picture;
		String pathMap = context.getCacheDir().getPath()+MapTask.MAP_PATH+items.get(position).picture;
		
		image.setImageBitmap(cap);
		ImageAsync loader = new ImageAsync(image,context);
		loader.execute(path);
		
		ImageAsync loaderMap = new ImageAsync(mapView,context);
		loaderMap.execute(pathMap);
		
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
