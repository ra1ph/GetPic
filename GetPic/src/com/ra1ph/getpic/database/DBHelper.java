package com.ra1ph.getpic.database;

import java.util.LinkedList;

import com.ra1ph.getpic.AsyncTask;
import com.ra1ph.getpic.Constants;
import com.ra1ph.getpic.MessageProcessor;
import com.ra1ph.getpic.message.Message;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private static volatile DBHelper instance;
	
	final static int DB_VER = 1;
    final static String DB_NAME = "database.db";
    final static String MESSAGE_TABLE_NAME = "messages";
    
    private MessageProcessor task;
    
    Context mContext;    
    
    public static DBHelper getInstance(Context context){
    	DBHelper localInstance = instance;
        if (localInstance == null) {
            synchronized (DBHelper.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DBHelper(context);
                }
            }
        }
        return localInstance;
    }
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VER);
        Log.d(Constants.DEBUG_TAG,"constructor called");
        mContext = context;
        task = new MessageProcessor(this.getWritableDatabase());
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.d(Constants.DEBUG_TAG,"onCreate() called");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	public void setWriteBlock(boolean isBlock){
		task.setBlock(isBlock);
	}
	
	public void addMessage(Message mes){
		synchronized(task.toWrite){
			task.toWrite.add(mes);
		}
	}

	
	public interface Writable{
		public void saveToDB(SQLiteDatabase db);
	}
	
}
