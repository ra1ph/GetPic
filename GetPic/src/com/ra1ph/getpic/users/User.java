package com.ra1ph.getpic.users;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.ra1ph.getpic.database.DBHelper.Writable;

public class User implements Writable {

	String user_id,picture;
	
	final static String USER_ID = "user_id";
	final static String PICTURE = "picure";
	final static String TABLE_NAME = "users";
	final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+
            " ( _id INTEGER PRIMARY KEY , user_id TEXT, picture TEXT)";
	
	@Override
	public void saveToDB(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		createTable(db);
		
		ContentValues val = new ContentValues();
		val.put(USER_ID, user_id);
		val.put(PICTURE, picture);
		
		db.insert(TABLE_NAME, null, val);
	}

	private void createTable(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
	}
	
}
