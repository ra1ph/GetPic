package com.ra1ph.getpic;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
    private final Context context;
    public ArrayList<Message> messages;

    public ChatAdapter(ArrayList<Message> messages, Context context){
        this.messages = messages;
        this.context = context;
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
        if(messages.get(position).type== Message.MessageType.TEXT){
        TextView text = (TextView) view.findViewById(R.id.chat_text);
        text.setText(messages.get(position).body);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) text.getLayoutParams();
        if(messages.get(position).direction==Message.DIRECTION_IN) params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        else if(messages.get(position).direction==Message.DIRECTION_OUT) params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        text.setLayoutParams(params);
        }else  {
            TextView text = (TextView) view.findViewById(R.id.chat_text);
            text.setText("");
        }
        return view;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
