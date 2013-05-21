package com.ra1ph.getpic;

import com.ra1ph.getpic.service.XMPPService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btn = (Button) findViewById(R.id.button);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(MainActivity.this, XMPPService.class);
				i.putExtra(XMPPService.CODE_ACTION, XMPPService.NEW_MESSAGE);
				i.putExtra(XMPPService.MESSAGE_TO, "ra1ph@jabber.ru");
				i.putExtra(XMPPService.MESSAGE_BODY, "Hello!");		
				startService(i);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
