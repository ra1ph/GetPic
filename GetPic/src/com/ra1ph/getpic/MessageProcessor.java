package com.ra1ph.getpic;

import java.util.LinkedList;

import com.ra1ph.getpic.database.DBHelper.Writable;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MessageProcessor extends com.ra1ph.getpic.AsyncTask<Void, Void, Void> {
	private static long TIME_SLEEP = 300;
	public LinkedList<Writable> toWrite;
	boolean isActive=true;
	private boolean isBlock=false;
	SQLiteDatabase db;
	Context context;
	
	public MessageProcessor(Context context, SQLiteDatabase db){
		this.db = db;
		this.context = context;
		toWrite = new LinkedList<Writable>();
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		// TODO Auto-generated method stub
		while(isActive){
			Writable write=null;
			synchronized(toWrite){
				write = toWrite.poll();				
				}
			if(write!=null){	
				synchronized(db){
				write.saveToDB(db);
				sendBroadcast(MainActivity.UPDATE_ALL);
				if(write instanceof com.ra1ph.getpic.message.Message)
				Log.d(Constants.DEBUG_TAG, "bla bla bla "+((com.ra1ph.getpic.message.Message)write).body);
				}
			} else
				try {
					Thread.sleep(TIME_SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
	
	public void stop(){
		isActive=false;
	}

	public boolean isBlock() {
		return isBlock;
	}

	public void setBlock(boolean isBlock) {
		this.isBlock = isBlock;
	}
	
	private void sendBroadcast(int ACTION){
		Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
		intent.putExtra(MainActivity.KEY_ACTION,ACTION);
        context.sendBroadcast(intent);
	}

}
