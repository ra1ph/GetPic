package com.ra1ph.getpic.message;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.Loader;

import com.ra1ph.getpic.AsyncTask;
import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.database.DBHelper.LoadListener;
import com.ra1ph.getpic.database.DBLoader;
import com.ra1ph.getpic.database.DBHelper.Loadable;
import com.ra1ph.getpic.database.DBHelper.Writable;

public class Message implements Writable, Loadable {
	public String user_id, body;
	int direction;

	public enum MessageType {
		TEXT, IMAGE
	};

	public MessageType type;

	final static String USER_ID = "user_id";
	final static String DIRECTION = "direction";
	final static String BODY = "body";
	final static String IS_PICTURE = "is_picture";
	public final static int DIRECTION_OUT = 0;
	public final static int DIRECTION_IN = 1;
	final static String TABLE_NAME = "messages";
	final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_NAME
			+ " ( _id INTEGER PRIMARY KEY , user_id TEXT, direction TEXT, body TEXT, is_picture BOOLEAN)";

	public Message(String user_id, String body, int direction) {
		this.body = body;
		this.direction = direction;
		this.user_id = user_id;
	}

	@Override
	public void saveToDB(SQLiteDatabase db) {
		// TODO Auto-generated method stub

		createTable(db);

		ContentValues val = new ContentValues();
		val.put(USER_ID, user_id);
		val.put(DIRECTION, direction);
		val.put(BODY, body);
		if (type == MessageType.TEXT)
			val.put(IS_PICTURE, false);
		else if (type == MessageType.IMAGE)
			val.put(IS_PICTURE, true);

		db.insert(TABLE_NAME, null, val);

	}

	private void createTable(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public Object processCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		ArrayList<Message> messages = new ArrayList<Message>();
		if(cursor.moveToFirst()){
			do{
				Message mes = new Message(cursor.getString(cursor.getColumnIndex(USER_ID)),cursor.getString(cursor.getColumnIndex(BODY)),cursor.getInt(cursor.getColumnIndex(DIRECTION)));
				if(cursor.getInt(cursor.getColumnIndex(IS_PICTURE))==1)mes.type=MessageType.IMAGE;
				else mes.type=MessageType.TEXT;
				messages.add(mes);
			}while(cursor.moveToNext());
		}
		
		return messages;
	}
	
	public void getMessages(LoadListener listener, DBHelper helper){
		DBLoader loader = new DBLoader(helper);
		loader.tableName=TABLE_NAME;
		loader.setListener(listener);
		loader.setProcessor(this);
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);	
	}
	
}
