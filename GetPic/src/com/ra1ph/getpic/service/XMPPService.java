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
	public static final int NEW_MESSAGE = 0x010;
	
	public static final String CODE_ACTION = "code_action";
	public static final String MESSAGE_TO = "message_to";
	public static final String MESSAGE_BODY = "message_body";
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		int code = intent.getIntExtra(CODE_ACTION, 0);
		switch(code){
		case NEW_MESSAGE:
			Message mes = new Message();
			mes.setBody(intent.getStringExtra(MESSAGE_BODY));
			mes.setTo(intent.getStringExtra(MESSAGE_TO));
			task.addMessage(mes);
			break;
		
		}
		return super.onStartCommand(intent, flags, startId);
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
