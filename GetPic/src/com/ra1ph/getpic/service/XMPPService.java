package com.ra1ph.getpic.service;

import org.jivesoftware.smack.packet.Message;

import com.ra1ph.getpic.AsyncTask;
import com.ra1ph.getpic.Constants;
import com.ra1ph.getpic.database.DBHelper;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

public class XMPPService extends Service {

	private static final String SERVICE_THREAD_NAME = "myThread";
	XMPPTask task;
	public static final int NEW_TEXT_MESSAGE = 0x010;
	public static final int NEW_IMAGE_MESSAGE = 0x020;
	
	public static final String CODE_ACTION = "code_action";
	public static final String MESSAGE_TO = "message_to";
	public static final String MESSAGE_BODY = "message_body";
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		com.ra1ph.getpic.message.Message mes;
		int code = intent.getIntExtra(CODE_ACTION, 0);
		switch(code){
		case NEW_TEXT_MESSAGE:
			mes = new com.ra1ph.getpic.message.Message(intent.getStringExtra(MESSAGE_TO), intent.getStringExtra(MESSAGE_BODY), com.ra1ph.getpic.message.Message.DIRECTION_OUT);
			mes.type = com.ra1ph.getpic.message.Message.MessageType.TEXT;
			task.addMessage(mes);
			break;
		case NEW_IMAGE_MESSAGE:
			mes = new com.ra1ph.getpic.message.Message(intent.getStringExtra(MESSAGE_TO), intent.getStringExtra(MESSAGE_BODY), com.ra1ph.getpic.message.Message.DIRECTION_OUT);
			mes.type = com.ra1ph.getpic.message.Message.MessageType.IMAGE;
			task.addMessage(mes);
			break;
		
		}
		return START_STICKY;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d(Constants.DEBUG_TAG, "onCreate");
		super.onCreate();
		task = new XMPPTask(this,"p00rGen","k_lt45mm");
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		/*
		helper = DBHelper.getInstance(this);
		connect();
		createChat("ra1ph@habber.ru");
		Log.d(Constants.DEBUG_TAG, "Service started");*/
		
	}
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
   
}
