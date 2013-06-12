package com.ra1ph.getpic;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.database.DBLoader;
import com.ra1ph.getpic.message.Message;
import com.ra1ph.getpic.service.XMPPService;
import com.ra1ph.getpic.users.User;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 11.06.13
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
public class ChatActivity extends SuperActivity implements DBHelper.LoadListener {
    public static final String KEY_ACTION = "chat_action";
    public static final int UPDATE = 0x0010;
    public static final int MESSAGE_SENDED = 0x0020;
    public static final String BROADCAST_ACTION = "com.ra1ph.getpic.broadcastchat";


    ChatAdapter adapter;
    ArrayList<Message> messages;
    private ProgressDialog progress;
    private BroadcastReceiver br;
    private String user_id;
    ListView list;
    EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.chat_layout);
        user_id = this.getIntent().getStringExtra(User.USER_ID);
        messages = new ArrayList<Message>();
        adapter = new ChatAdapter(messages,this);
        list = (ListView) findViewById(R.id.chat_list);
        list.setAdapter(adapter);
        list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        Message.getMessages(this, DBHelper.getInstance(this), user_id);

        Button send = (Button) findViewById(R.id.send_message_button);
        text = (EditText) findViewById(R.id.message_input);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //To change body of implemented methods use File | Settings | File Templates.
                Message mesage = new Message(user_id,text.getText().toString(),Message.DIRECTION_OUT, DBHelper.Writable.ADD);
                sendMessage(mesage);
            }
        });

        registerBroadcast();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
        unregisterBroadcast();
    }

    private void unregisterBroadcast(){
        unregisterReceiver(br);
    }

    private void registerBroadcast(){
        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int action = intent.getIntExtra(KEY_ACTION, -1);
                switch(action){
                    case UPDATE:
                        Message.getMessages(ChatActivity.this, DBHelper.getInstance(ChatActivity.this), user_id);
                        break;
                    case MESSAGE_SENDED:
                        progress.dismiss();
                        text.setText("");
                        break;
                }
            }
        };
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(br, intFilt);
    }

    private void sendMessage(Message mes) {
        progress= ProgressDialog.show(this, "Please wait", "Loading please wait..", true);
        progress.setCancelable(false);

        Intent i = new Intent(ChatActivity.this, XMPPService.class);
        i.putExtra(XMPPService.CODE_ACTION, XMPPService.NEW_TEXT_MESSAGE);
        i.putExtra(XMPPService.MESSAGE_TO,mes.user_id);
        i.putExtra(XMPPService.MESSAGE_BODY,mes.body);
        startService(i);
    }


    @Override
    public void onLoadListener(Object object) {
        //To change body of implemented methods use File | Settings | File Templates.
        messages = (ArrayList<Message>) object;
        adapter.messages = messages;
        adapter.notifyDataSetChanged();
        list.setStackFromBottom(true);
    }
}
