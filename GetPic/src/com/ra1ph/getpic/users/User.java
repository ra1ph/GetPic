package com.ra1ph.getpic.users;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ra1ph.getpic.AsyncTask;
import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.database.DBHelper.LoadListener;
import com.ra1ph.getpic.database.DBHelper.Loadable;
import com.ra1ph.getpic.database.DBHelper.Writable;
import com.ra1ph.getpic.database.DBLoader;
import com.ra1ph.getpic.message.Message;
import com.ra1ph.getpic.message.Message.MessageType;

public class User implements Writable,Loadable {

	public String user_id,picture;
	
	final static String USER_ID = "user_id";
	final static String PICTURE = "picture";
	final static String TABLE_NAME = "users";
	final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+
            " ( _id INTEGER PRIMARY KEY , user_id TEXT, picture TEXT)";
	
	public User(String user_id, String picture) {
		// TODO Auto-generated constructor stub
		this.picture=picture;
		this.user_id=user_id;
	}
	
	@Override
	public void saveToDB(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createTable(db);
		
		ContentValues val = new ContentValues();
		val.put(USER_ID, user_id);
		val.put(PICTURE, picture);
		
		db.rawQuery("DELETE FROM "+TABLE_NAME+" WHERE "+USER_ID+"='"+user_id+"'", null);
		db.insert(TABLE_NAME, null, val);
	}

	private void createTable(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public Object processCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		ArrayList<User> users = new ArrayList<User>();
		if(cursor.moveToFirst()){
			do{
				User user = new User(cursor.getString(cursor.getColumnIndex(USER_ID)),cursor.getString(cursor.getColumnIndex(PICTURE)));

				users.add(user);
			}while(cursor.moveToNext());
		}
		
		return users;
	}
	
	public void getUsers(LoadListener listener,DBHelper helper){
		DBLoader loader = new DBLoader(helper);
		loader.tableName=TABLE_NAME;
		loader.setListener(listener);
		loader.setProcessor(this);
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);		
	}
}
