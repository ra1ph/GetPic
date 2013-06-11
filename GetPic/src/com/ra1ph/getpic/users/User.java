package com.ra1ph.getpic.users;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ra1ph.getpic.AsyncTask;
import com.ra1ph.getpic.Constants;
import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.database.DBHelper.LoadListener;
import com.ra1ph.getpic.database.DBHelper.Loadable;
import com.ra1ph.getpic.database.DBHelper.Writable;
import com.ra1ph.getpic.database.DBLoader;
import com.ra1ph.getpic.message.Message;
import com.ra1ph.getpic.message.Message.MessageType;

public class User implements Writable,Loadable {

	public String user_id,picture;
	private int Type;
	
	public final static String USER_ID = "user_id";
	final static String PICTURE = "picture";
	final static String TABLE_NAME = "users";
	final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+
            " ( _id INTEGER PRIMARY KEY , user_id TEXT, picture TEXT)";
	
	public User(String user_id, String picture,int typeOp) {
		// TODO Auto-generated constructor stub
		this.picture=picture;
		this.user_id=user_id;
		this.Type = typeOp;
	}
	
	@Override
	public void saveToDB(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		if(this.Type == ADD){
		ContentValues val = new ContentValues();
		val.put(USER_ID, user_id);
		val.put(PICTURE, picture);
		
		String query="DELETE FROM "+TABLE_NAME+" WHERE KEY_ROWID="+USER_ID+" and KEY_NAME='"+user_id+"'";
		Log.d("myLog", query);
		db.delete(TABLE_NAME, USER_ID+"=?",new String[]{user_id});
		db.insert(TABLE_NAME, null, val);
		}else if(this.Type == DELETE){
			Log.d(Constants.DEBUG_TAG,"Delete user "+user_id);
			db.delete(TABLE_NAME, USER_ID+"=?",new String[]{user_id});
		}
	}

	public static void createTable(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public Object processCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		ArrayList<User> users = new ArrayList<User>();
		if(cursor.moveToFirst()){
			do{
				User user = new User(cursor.getString(cursor.getColumnIndex(USER_ID)),cursor.getString(cursor.getColumnIndex(PICTURE)),Writable.ADD);

				users.add(user);
			}while(cursor.moveToNext());
		}
		
		return users;
	}
	
	public static void getUsers(LoadListener listener,DBHelper helper){
		DBLoader loader = new DBLoader(helper);
		User user = new User("", "",Writable.ADD);
		loader.tableName=TABLE_NAME;
		loader.setListener(listener);
		loader.setProcessor(user);
		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);		
	}

	@Override
	public void setType(int Type) {
		// TODO Auto-generated method stub
		this.Type = Type;
	}
	
	public static void deleteUser(String user_id, DBHelper helper){
		helper.addWritable(new User(user_id,"",Writable.DELETE));
	}
	
}
