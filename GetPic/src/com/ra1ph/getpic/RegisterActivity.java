package com.ra1ph.getpic;

import com.ra1ph.getpic.service.XMPPService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends Activity {
	
	SharedPreferences mPrefs;
	BroadcastReceiver br;
	
	public final static String REG_BROADCAST_ACTION = "com.ra1ph.getpic.broadcastreg";
	public static final int SUCCESS = 0x0010;
	public static final int FAIL = 0x0020;
	public static final int ALREDY_CONNECTED = 0x0030;
	public static final int CONNECTION_FAIL = 0x0040;
	
	public static final String LOGIN = "login";
	public static final String PASS = "password";
	public static final String REG = "reg";
	public static final String EMAIL = "email";
	public static final String IS_LOGOUT = "is_logout";
	
	String log,password,mail;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.registration);
		
		super.onCreate(savedInstanceState);
		mPrefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
		Button register = (Button) findViewById(R.id.register_button);
		register.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			
				EditText email = (EditText) findViewById(R.id.email);
				EditText login = (EditText) findViewById(R.id.login);
				EditText pass = (EditText) findViewById(R.id.pass);
				
				log = login.getText().toString();
				password = pass.getText().toString();
				mail = email.getText().toString();
				sendReg(log, password,mail);
			}
		});
	}
	
	
	private void sendReg(String login, String pass, String email) {
		Intent i = new Intent(RegisterActivity.this, XMPPService.class);
		i.putExtra(XMPPService.CODE_ACTION, XMPPService.REG_USER);
		i.putExtra(LOGIN, login);
		i.putExtra(PASS, pass);
		i.putExtra(EMAIL, email);
		startService(i);
	}
	
	private void unregisterBroadcast() {
		unregisterReceiver(br);
	}

	private void registerBroadcast() {
		br = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				int action = intent.getIntExtra(REG, -1);
				switch (action) {
				case SUCCESS:
					mPrefs.edit().putString(LOGIN, log)
					.putString(PASS, password)
					.commit();
					nextActivity();
					break;

				case CONNECTION_FAIL:
					Log.d(Constants.DEBUG_TAG, "Connection FAIL!!");
					break;
				case FAIL:
					Log.d(Constants.DEBUG_TAG, "Auth FAIL!!");
					break;
				case ALREDY_CONNECTED:
					nextActivity();
					break;

				}
			}
		};
		IntentFilter intFilt = new IntentFilter(REG_BROADCAST_ACTION);
		registerReceiver(br, intFilt);
	}
	
	private void nextActivity() {
		Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
		finish();
	}
}
