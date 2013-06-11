package com.ra1ph.getpic.database;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ra1ph.getpic.AsyncTask;
import com.ra1ph.getpic.database.DBHelper.LoadListener;
import com.ra1ph.getpic.database.DBHelper.Loadable;
import com.ra1ph.getpic.message.Message;

public class DBLoader extends AsyncTask<Void, Void, Object>{
	public String tableName = null, selection = null;
	public String[] selArgs = null, columns = null;
	DBHelper db;
	LoadListener listener;
	Loadable processor;
	
	public DBLoader(DBHelper db) {
		// TODO Auto-generated constructor stub
		this.db = db;
	}
	
	public void setListener(LoadListener listener){
		this.listener = listener;
	}
	
	public void setProcessor(Loadable processor){
		this.processor = processor;
	}
	
	@Override
	protected Object doInBackground(Void... params) {
		// TODO Auto-generated method stub
		db.setWriteBlock(true);
		Cursor cursor = db.getWritableDatabase().query(tableName, columns, selection, selArgs, null, null, null);
		db.setWriteBlock(false);
		Object object = processor.processCursor(cursor);
		return object;
	}
	
	@Override
	protected void onPostExecute(Object object) {
		// TODO Auto-generated method stub
		listener.onLoadListener(object);
		db.setWriteBlock(false);
		super.onPostExecute(object);
	}
}
