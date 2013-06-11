package com.ra1ph.getpic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
public class ChatActivity extends SuperActivity {
    ChatAdapter adapter;
    ArrayList<Message> messages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setContentView(R.layout.chat_layout);
        Message mes = new Message(null,null,0,0);
        final String user_id = this.getIntent().getStringExtra(User.USER_ID);
        messages = new ArrayList<Message>();
        adapter = new ChatAdapter(messages,this);
        ListView list = (ListView) findViewById(R.id.chat_list);
        list.setAdapter(adapter);
        mes.getMessages(new DBHelper.LoadListener() {
            @Override
            public void onLoadListener(Object object) {
                //To change body of implemented methods use File | Settings | File Templates.
                messages = (ArrayList<Message>) object;
                adapter.messages = messages;
                adapter.notifyDataSetChanged();
            }
        }, DBHelper.getInstance(this), user_id);

        Button send = (Button) findViewById(R.id.send_message_button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //To change body of implemented methods use File | Settings | File Templates.
                TextView text = (TextView) findViewById(R.id.message_input);
                Message mesage = new Message(user_id,text.getText().toString(),Message.DIRECTION_OUT, DBHelper.Writable.ADD);
                sendMessage(mesage);
            }
        });

    }

    private void sendMessage(Message mes) {
        Intent i = new Intent(ChatActivity.this, XMPPService.class);
        i.putExtra(XMPPService.CODE_ACTION, XMPPService.NEW_TEXT_MESSAGE);
        i.putExtra(XMPPService.MESSAGE_TO,mes.user_id);
        i.putExtra(XMPPService.MESSAGE_BODY,mes.body);
        startService(i);
    }


}
