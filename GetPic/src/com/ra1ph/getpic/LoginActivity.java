package com.ra1ph.getpic;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.ra1ph.getpic.MainActivity;
import com.ra1ph.getpic.utils.DialogManager;

public class LoginActivity extends Activity {

	public static final String LOGIN = "login";
	public static final String PASS = "password";
	public static final String AUTH = "auth";
	public static final String IS_LOGOUT = "is_logout";

	public static final int SUCCESS = 0x0010;
	public static final int FAIL = 0x0020;
	public static final int ALREDY_CONNECTED = 0x0030;
	public static final int CONNECTION_FAIL = 0x0040;

	SharedPreferences mPrefs;
	BroadcastReceiver br;
	String login,pass;
    ProgressDialog progress;
    boolean isLogout;
    EditText log;
    EditText password;

    public final static String BROADCAST_ACTION = "com.ra1ph.getpic.broadcastauth";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.login);
		registerBroadcast();
		isLogout = getIntent().getBooleanExtra(IS_LOGOUT, false);
		mPrefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
		log = (EditText) findViewById(R.id.login);
		password = (EditText) findViewById(R.id.pass);
		login = mPrefs.getString(LOGIN, null);
		pass = mPrefs.getString(PASS, null);
        if(isLogout){
            mPrefs.edit()
                    .remove(LOGIN)
                    .remove(PASS)
                    .commit();
        }
		if ((login != null) && (pass != null)) {
			log.setText(login);
			password.setText(pass);
			if(!isLogout)sendAuth(login, pass);
		} 
		
		Button sign = (Button) findViewById(R.id.sign);
		sign.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				login = log.getText().toString();
				pass = password.getText().toString();
				sendAuth(login, pass);
			}
		});
		
		Button reg = (Button) findViewById(R.id.register);
		reg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(LoginActivity.this,RegisterActivity.class);
				startActivity(i);
			}
		});

		super.onCreate(savedInstanceState);
	}

	private void unregisterBroadcast() {
		unregisterReceiver(br);
	}

	private void registerBroadcast() {
		br = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
                if(progress!=null)progress.dismiss();
				int action = intent.getIntExtra(AUTH, -1);
				switch (action) {
				case SUCCESS:
					mPrefs.edit().putString(LOGIN, login)
					.putString(PASS, pass)
					.commit();
					nextActivity();
					break;

				case CONNECTION_FAIL:
                    DialogManager.errorDialog(LoginActivity.this,getResources().getString(R.string.error_internet));
					Log.d(Constants.DEBUG_TAG, "Connection FAIL!!");
					break;
				case FAIL:
                    DialogManager.wrongAuthDialog(LoginActivity.this);
					Log.d(Constants.DEBUG_TAG, "Auth FAIL!!");
					break;
				case ALREDY_CONNECTED:
					nextActivity();
					break;

				}
			}
		};
		IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
		registerReceiver(br, intFilt);
	}

    @Override
    protected void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        login = mPrefs.getString(LOGIN, null);
        pass = mPrefs.getString(PASS, null);
        if ((login != null) && (pass != null)) {
            log.setText(login);
            password.setText(pass);
            sendAuth(login, pass);
        }
    }

    private void sendAuth(String login, String pass) {
        progress=ProgressDialog.show(this, "Please wait", "Loading please wait..", true);
        progress.setCancelable(false);

		Intent i = new Intent(LoginActivity.this, XMPPService.class);
		i.putExtra(XMPPService.CODE_ACTION, XMPPService.AUTH_USER);
		i.putExtra(LOGIN, login);
		i.putExtra(PASS, pass);
		startService(i);
	}

	private void nextActivity() {
		Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
        finish();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterBroadcast();
		super.onDestroy();
	}
}
