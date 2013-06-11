package com.ra1ph.getpic;

import android.content.*;
import com.google.android.maps.MapActivity;
import com.ra1ph.getpic.service.XMPPService;
import com.ra1ph.getpic.users.User;

import net.simonvt.menudrawer.MenuDrawer;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SuperActivity extends Activity {

	public static MenuDrawer mMenuDrawer;
	ArrayAdapter<String> adapter;
	private static final int CLEAR_ALL = 0;
	private static final int ABOUT = 1;
	private static final int LOGOUT = 2;
	BroadcastReceiver br2;

	public final static String LOGOUT_BROADCAST_ACTION = "com.ra1ph.getpic.broadcastlogout";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		registerBroadcast();
		mMenuDrawer = MenuDrawer.attach(this);
		mMenuDrawer.setMenuView(R.layout.menu);
		createMenu();
		super.onCreate(savedInstanceState);
	}

	public void createMenu() {
		String[] array = new String[] { "Clear all", "About", "Log out" };
		adapter = new ArrayAdapter<String>(this, R.layout.menu_item,
				R.id.menu_item, array);
		ListView list = (ListView) findViewById(R.id.menu_list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case CLEAR_ALL:
					break;
				case ABOUT:
					break;
				case LOGOUT:
					sendLogOut();
					break;
				}
			}

		});

	}

	private void sendLogOut() {
		Intent i = new Intent(SuperActivity.this, XMPPService.class);
		i.putExtra(XMPPService.CODE_ACTION, XMPPService.LOG_OUT);
		startService(i);
	}

	private void unregisterBroadcast() {
		unregisterReceiver(br2);
	}

	private void registerBroadcast() {
		br2 = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				Intent i = new Intent(SuperActivity.this, LoginActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				i.putExtra(LoginActivity.IS_LOGOUT, true);
				startActivity(i);
			}
		};
		IntentFilter intFilt = new IntentFilter(LOGOUT_BROADCAST_ACTION);
		registerReceiver(br2, intFilt);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterBroadcast();
		super.onDestroy();
	}

    public String getUserLogin(){
        SharedPreferences mPrefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        return mPrefs.getString(LoginActivity.LOGIN, null);
    }

    public String getUserPass(){
        SharedPreferences mPrefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        return mPrefs.getString(LoginActivity.PASS, null);
    }


}
