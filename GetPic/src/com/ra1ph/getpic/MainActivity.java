package com.ra1ph.getpic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import android.app.ProgressDialog;
import android.content.*;
import android.net.Uri;
import android.provider.MediaStore;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ra1ph.getpic.utils.DialogManager;
import net.simonvt.menudrawer.MenuDrawer;

import com.ra1ph.getpic.database.DBHelper;
import com.ra1ph.getpic.database.DBHelper.LoadListener;
import com.ra1ph.getpic.image.EXIFProcessor;
import com.ra1ph.getpic.service.GPSTracker;
import com.ra1ph.getpic.service.XMPPService;
import com.ra1ph.getpic.users.User;

import android.os.Bundle;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends SuperActivity implements LoadListener {

    public static final String PREFS_NAME = "prefs";
    public static final String ERROR_CODE = "errorCode";
    private static final int CAMERA_PIC_REQUEST = 2500;
    private static final int PICTURE_RESULT = 9;
    private static final String TEMP_FILENAME = "temp";
    public static final String PROGRESS_VALUE = "progressValue";
    private String send_user_id = null;
    private SharedPreferences mPrefs;
    private PullToRefreshListView imageList;
    DBHelper helper;
    ArrayList<User> items;
    ImageListAdapter adapter;
    BroadcastReceiver br;

    GPSTracker gps;

    public final static String BROADCAST_ACTION = "com.ra1ph.getpic.broadcastupdate";
    public final static String KEY_ACTION = "action";
    public final static int UPDATE_ALL = 0x0010;
    public static final int PHOTO_SENDED = 0x0020;
    public static final int PROGRESS_UPDATE = 0x0030;
    public static final int PHOTO_RECEIVED = 0x0040;
    public static final int ERROR = 0x0050;

    public static final String BOT_JID = "getpicbot@ua0022903/Smack";
    private static final String BOT_JID1 = "kakaka1@localhost/Smack";
    private static final String BOT_JID2 = "ra1ph@localhost/Smack";
    private ProgressDialog progress;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMenuDrawer.setContentView(R.layout.activity_main);
        /*MenuFragment menu = (MenuFragment)getSupportFragmentManager().findFragmentById(R.id.f_menu);
		menu.getListView().setOnItemClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String login = mPrefs.getString(LoginActivity.LOGIN, "");

        /*if (login.equals("kakaka1")) BOT_JID = BOT_JID2;
        else if (login.toLowerCase().equals("ra1ph")) BOT_JID = BOT_JID1;    */

        imageList = (PullToRefreshListView) findViewById(R.id.image_list);
        helper = new DBHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        items = new ArrayList<User>();
        adapter = new ImageListAdapter(this, items);
        imageList.setAdapter(adapter);

        User.getUsers(this, helper);

        Intent i = new Intent(MainActivity.this, XMPPService.class);
        startService(i);

        ImageView shot = (ImageView) findViewById(R.id.shot_btn);
        shot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openCamera(BOT_JID);
            }
        });

        registerBroadcast();
        gps = new GPSTracker(this);
        if (!gps.canGetLocation()) Log.d(Constants.DEBUG_TAG, "GPS IS NOT ENABLED!!!");

        imageList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                //To change body of implemented methods use File | Settings | File Templates.
                sendGetPic();
            }
        });
        imageList.setPullLabel(this.getResources().getString(R.string.pull_label));
        imageList.setReleaseLabel(this.getResources().getString(R.string.release_label));

    }

    private void sendGetPic(){
        Intent i = new Intent(MainActivity.this, XMPPService.class);
        i.putExtra(XMPPService.CODE_ACTION, XMPPService.GET_PICTURE);
        startService(i);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        unregisterBroadcast();

        super.onDestroy();
    }

    private void unregisterBroadcast() {
        unregisterReceiver(br);
    }

    private void registerBroadcast() {
        br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int action = intent.getIntExtra(KEY_ACTION, -1);
                switch (action) {
                    case UPDATE_ALL:
                        User.getUsers(MainActivity.this, helper);
                        Log.d("myLog", "updated all");
                        break;
                    case PHOTO_SENDED:
                        progress.dismiss();
                        break;
                    case PROGRESS_UPDATE:
                        int prog = intent.getIntExtra(PROGRESS_VALUE,0);
                        progress.setProgress(prog);
                        break;
                    case PHOTO_RECEIVED:
                        imageList.onRefreshComplete();
                        User.getUsers(MainActivity.this, helper);
                        break;
                    case ERROR:
                        DialogManager.errorDialog(MainActivity.this,intent.getStringExtra(ERROR_CODE));
                        break;
                }
            }
        };
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(br, intFilt);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if ((requestCode == PICTURE_RESULT) && (resultCode == RESULT_OK) && (imageUri!=null)) {
            Bitmap image = null;
            try {
                image = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                String user_id = mPrefs.getString(XMPPService.MESSAGE_TO, null);
                String filename = BMPtoFile(image);
                sendImage(user_id, filename);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String BMPtoFile(Bitmap bitmap) {
        File f = new File(this.getExternalCacheDir(), UUID.randomUUID().toString());
        try {
            f.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 75, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);

            EXIFProcessor exif = new EXIFProcessor(f);
            exif.UpdateGeoTag(gps.getLatitude(), gps.getLongitude());

            return f.getName();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    public void openCamera(String user_id) {

        mPrefs.edit().putString(XMPPService.MESSAGE_TO, user_id).commit();
        imageUri=null;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PICTURE_RESULT);


        //startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    public void sendImage(String user_id, String filename) {
        progress = new ProgressDialog(MainActivity.this);
        progress.setMax(100);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setCancelable(false);
        progress.show();

        Intent i = new Intent(MainActivity.this, XMPPService.class);
        i.putExtra(XMPPService.CODE_ACTION, XMPPService.NEW_IMAGE_MESSAGE);
        i.putExtra(XMPPService.MESSAGE_TO, user_id);
        i.putExtra(XMPPService.MESSAGE_BODY, filename);
        startService(i);
    }

    @Override
    public void onLoadListener(Object object) {
        // TODO Auto-generated method stub
        items = (ArrayList<User>) object;
        adapter.items = items;
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        gps = new GPSTracker(this);
        if (!gps.canGetLocation()) Log.d(Constants.DEBUG_TAG, "GPS IS NOT ENABLED!!!");
        super.onResume();
    }

}
