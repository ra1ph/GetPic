package com.ra1ph.getpic;

import android.app.Activity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.ra1ph.getpic.message.Message;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 11.06.13
 * Time: 17:30
 * To change this template use File | Settings | File Templates.
 */
public class ChatAdapter extends BaseAdapter {
    private final Activity context;
    public ArrayList<Message> messages;
    public int maxWidth,maxHeight;
    ImageLoader imageLoader;

    public ChatAdapter(ArrayList<Message> messages, Activity context){
        this.messages = messages;
        this.context = context;
        Display display = context.getWindowManager().getDefaultDisplay();
        maxWidth = display.getWidth()/2;
        maxHeight = display.getHeight()/2;

        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(context);
        imageLoader.init(config);
    }

    @Override
    public int getCount() {
        return messages.size();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getItemId(int position) {
        return position;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflator = ((Activity)context).getLayoutInflater();
            view = inflator.inflate(R.layout.chatlist_item, null);
        } else {
            view = convertView;
        }

        LinearLayout lay = (LinearLayout) view.findViewById(R.id.chat_lay);
        TextView text = (TextView) view.findViewById(R.id.chat_text);
        ImageView image = (ImageView) view.findViewById(R.id.chat_image);

        if(messages.get(position).type== Message.MessageType.TEXT){
        text.setText(messages.get(position).body);
        image.setImageResource(0);
            image.setImageDrawable(null);
        }else if(messages.get(position).type== Message.MessageType.IMAGE) {
        String path = "file://"+context.getExternalCacheDir().getPath()+"/"+messages.get(position).body;

        /*ImageAsync loader = new ImageAsync(image,context,maxWidth,maxHeight);
        loader.execute(path);*/

            DisplayImageOptions options1 = new     DisplayImageOptions.Builder().showStubImage(R.drawable.ic_launcher)
                    .showImageForEmptyUri(R.drawable.ic_launcher).cacheInMemory().cacheOnDisc().build();
            imageLoader.displayImage(path,image,options1);

        text.setText("");
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if(messages.get(position).direction==Message.DIRECTION_IN) params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        else if(messages.get(position).direction==Message.DIRECTION_OUT) params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lay.setLayoutParams(params);

        return view;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
