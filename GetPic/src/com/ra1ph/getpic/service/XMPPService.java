package com.ra1ph.getpic.service;

import org.jivesoftware.smack.packet.Message;

import com.ra1ph.getpic.AsyncTask;
import com.ra1ph.getpic.Constants;
import com.ra1ph.getpic.LoginActivity;
import com.ra1ph.getpic.MainActivity;
import com.ra1ph.getpic.RegisterActivity;
import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.database.DBHelper.Writable;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
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
	public static final int AUTH_USER = 0x030;
	public static final int LOG_OUT = 0x040;
	public static final int REG_USER = 0x050;

	public static final String CODE_ACTION = "code_action";
	public static final String MESSAGE_TO = "message_to";
	public static final String MESSAGE_BODY = "message_body";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (intent != null) {
			com.ra1ph.getpic.message.Message mes;
			int code = intent.getIntExtra(CODE_ACTION, 0);
			switch (code) {
			case NEW_TEXT_MESSAGE:
				mes = new com.ra1ph.getpic.message.Message(
						intent.getStringExtra(MESSAGE_TO),
						intent.getStringExtra(MESSAGE_BODY),
						com.ra1ph.getpic.message.Message.DIRECTION_OUT,Writable.ADD);
				mes.type = com.ra1ph.getpic.message.Message.MessageType.TEXT;
				task.addMessage(mes);
				break;
			case NEW_IMAGE_MESSAGE:
				mes = new com.ra1ph.getpic.message.Message(
						intent.getStringExtra(MESSAGE_TO),
						intent.getStringExtra(MESSAGE_BODY),
						com.ra1ph.getpic.message.Message.DIRECTION_OUT,Writable.ADD);
				mes.type = com.ra1ph.getpic.message.Message.MessageType.IMAGE;
				task.addMessage(mes);
				break;
			
			case AUTH_USER:
				String login = intent.getStringExtra(LoginActivity.LOGIN);
				String pass = intent.getStringExtra(LoginActivity.PASS);
				startConnection(login, pass);
				break;
				
			case LOG_OUT:
				task.isLogout.set(true);
				break;
				
			case REG_USER:
				String login_reg = intent.getStringExtra(RegisterActivity.LOGIN);
				String pass_reg = intent.getStringExtra(RegisterActivity.PASS);
				String email = intent.getStringExtra(RegisterActivity.EMAIL);
				registerUser(login_reg, pass_reg,email);
				break;
			}
		}
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d(Constants.DEBUG_TAG, "onCreate");
		super.onCreate();

	}
	
	public void registerUser(String login, String pass, String email){
		if ((task == null)||(!task.isActive.get())) {
			task = new XMPPTask(this, login, pass);
			task.email=email;
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,XMPPTask.ACTION_REGISTER);
		}else sendRegBroadcast(getApplicationContext(),RegisterActivity.ALREDY_CONNECTED);
	}
	
	public void startConnection(String login, String pass){
		if ((task == null)||(!task.isActive.get())) {
			task = new XMPPTask(this, login, pass);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,XMPPTask.ACTION_LOGIN);
		}else sendBroadcast(getApplicationContext(),LoginActivity.ALREDY_CONNECTED);
	}
	
	public static void sendBroadcast(Context context, int ACTION){
		Intent intent = new Intent(LoginActivity.BROADCAST_ACTION);
		intent.putExtra(LoginActivity.AUTH,ACTION);
        context.sendBroadcast(intent);
	}
	
	public static void sendRegBroadcast(Context context, int ACTION){
		Intent intent = new Intent(RegisterActivity.REG_BROADCAST_ACTION);
		intent.putExtra(RegisterActivity.REG,ACTION);
        context.sendBroadcast(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
