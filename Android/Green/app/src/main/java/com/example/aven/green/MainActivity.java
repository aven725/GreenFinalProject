package com.example.aven.green;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // DB
    private MyDB db = null;

    // Cursor 接查詢結果用
    Cursor cursor;

    // Data
    String[] str_data = {"NULL","NULL","NULL"};

    // ListView
    private String[] libId = {"NULL"}, libTime = {"NULL"},
            lib_G = {"NULL"},lib_B = {"NULL"},
            lib_C = {"NULL"},libType = {"NULL"};
    private int[] libNumber = {1};

    // 圖片
    int[] imageIds = {R.drawable.bulletin_activity, R.drawable.bulletin_other};

    // 自訂Adater
    MyAdapter adapter = new MyAdapter();

    ListView listContent;

    // Bluetooth
    private static BluetoothAdapter mBluetoothAdapter = null; // 用來搜尋、管理藍芽裝置
    private static BluetoothSocket mBluetoothSocket = null; // 用來連結藍芽裝置、以及傳送指令
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // 一定要是這組
    private static OutputStream mOutputStream = null;
    private static InputStream mInputStream = null;
    private final int REQUEST_ENABLE_BT = 1;
    private BroadcastReceiver myBluetoothBroadcastReceiver = null;

    private Button btnConnect;
    private Button btnDisconnect;

    private TextView txt_G;
    private TextView txt_B;
    private TextView txt_Co;

    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        //libId = new String[10];
        //libTime = new String[10];
        //libContent = new String[10];
        //libTitle = new String[10];
        //libType = new String[10];
        //libNumber = new int[10];

//        for (int i = 0; i < 10; i++) {
//            libId[i] = "libId" + i;
//            libTime[i] = "libTime" + i;
//            libContent[i] = "libContent" + i;
//            libTitle[i] = "libTitle" + i;
//            libType[i] = "libType" + i;
//            libNumber[i] = i;
//        }

        /**************
                ** Get view id
                **************/
        // Button
        btnConnect = (Button) findViewById(R.id.btnConnectDevice);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);

        // Listview
        listContent = (ListView) findViewById(R.id.list_result);

        // TextView
        txt_G = (TextView)findViewById(R.id.txt_G);
        txt_B = (TextView)findViewById(R.id.txt_B);
        txt_Co = (TextView)findViewById(R.id.txt_Co);

        /**************
                ** Register ClickListener
                **************/
        // Button
        btnConnect.setOnClickListener(btnClick);
        btnDisconnect.setOnClickListener(btnClick);

        // ListView聆聽事件
        listContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Toast.makeText(MainActivity.this, "no:" + libId[arg2],
                        Toast.LENGTH_SHORT).show();
                // 換頁+傳遞資料
//                Intent intent = new Intent();
//                intent.setClass(Bulletin.this, bulletin_content.class);
//                String pid = libId[arg2];
//                String ptime = libTime[arg2];
//                String ptitle = libTitle[arg2];
//                String pContent = libContent[arg2];
//                // 傳值
//                Bundle bundle = new Bundle();
//                bundle.putString("ID", pid);
//                bundle.putString("TIME", ptime);
//                bundle.putString("TITLE", ptitle);
//                bundle.putString("CONTENT", pContent);
//                // bundle.putInt("BulletinId",arg2);
//                intent.putExtras(bundle);
//                startActivity(intent);
            }
        });

        // 開啟DB
        db = new MyDB(this);
        db.open();

        //db.append2("11","22","33");
        //db.append2("11","22","33");
        //db.delete(138);

        upDataListView();

    }

    // 更新資料
    void upDataListView()
    {
        cursor = db.getAll();
        //cursor.moveToFirst();
        //Log.e("cursor", cursor.getCount()+"");
        //db.clearAll();
        int ows_num = cursor.getCount();
        if( ows_num > 0 )
        {

            libId = new String[ows_num];
            libTime = new String[ows_num];
            lib_G = new String[ows_num];
            lib_B = new String[ows_num];
            lib_C = new String[ows_num];
            libType = new String[ows_num];
            libNumber = new int[ows_num];

            cursor.moveToFirst();

            for( int i =0 ; i < cursor.getCount();i++ )
            {
                //Log.e("While ", "While");

                libId[i] = cursor.getString(0);
                lib_G[i] = cursor.getString(1);
                lib_B[i] = cursor.getString(2);
                lib_C[i] = cursor.getString(3);
                libTime[i] = cursor.getString(4);
                libType[i] = cursor.getString(0);
                libNumber[i] = cursor.getInt(0);
                cursor.moveToNext();
            }
        }
        else
        {
            libId = new String[1];
            libTime = new String[1];
            lib_G = new String[1];
            lib_B = new String[1];
            lib_C = new String[1];
            libType = new String[1];
            libNumber = new int[1];

            libId[0] = "1";
            libTime[0] = "none";
            lib_G[0] = "none";
            lib_B[0] = "none";
            lib_C[0] = "none";
            libType[0] = "none";
            libNumber[0] = 1;

        }
        // 使用自訂版面
        listContent.setAdapter(adapter);
    }

    // 藍芽接收資料
    void beginListenForDate()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10;

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while( !Thread.currentThread().isInterrupted() && !stopWorker ) {
                    try {
                        int bytesAvailable = mInputStream.available();

                        if( bytesAvailable > 0 )
                        {
                            //Log.e("bytesAvailable:",bytesAvailable+"");

                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);

                            //Log.e("mmInStream",new String(packetBytes));

                            for( int i = 0; i<bytesAvailable;i++ )
                            {
                                byte b = packetBytes[i];
                                if( b == '\n' )
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer,0,encodedBytes,0,encodedBytes.length);
                                    final String data = new String( encodedBytes,"US-ASCII" );
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            String str_temp = data;

                                            // 將收到的資料切割，並存到 str_data
                                            String[] d_value = str_temp.split(",");
                                            System.arraycopy(d_value, 0, str_data, 0, d_value.length);

                                            if (Double.parseDouble(str_data[0]) > 100) {
                                                txt_G.setTextColor(Color.RED); // red
                                            } else {
                                                txt_G.setTextColor(Color.BLUE); // blue
                                            }

                                            if (Double.parseDouble(str_data[1]) > 100) {
                                                txt_B.setTextColor(Color.RED); // red
                                            } else {
                                                txt_B.setTextColor(Color.BLUE); // blue
                                            }

                                            if (Double.parseDouble(str_data[2]) > 100) {
                                                txt_Co.setTextColor(Color.RED); // red
                                            } else {
                                                txt_Co.setTextColor(Color.BLUE); // blue
                                            }

                                            txt_G.setText(str_data[0]);
                                            txt_B.setText(str_data[1]);
                                            txt_Co.setText(str_data[2]);

                                            db.append2(str_data[0], str_data[1], str_data[2]);
                                            upDataListView();
                                        }
                                    });

                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }

                        }

                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    // 自訂ListView
    public class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return libTime.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return libTime[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            /** 如果頁面不存在就去抓 */
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.bulletin_listview2, null);
            }
            /** 取得自訂item的元件 */
            ImageView imgSelect = ((ImageView) convertView
                    .findViewById(R.id.imgSelect));
            TextView txtG = ((TextView) convertView
                    .findViewById(R.id.txt_G));
            TextView txtB = ((TextView) convertView
                    .findViewById(R.id.txt_B));
            TextView txtC = ((TextView) convertView
                    .findViewById(R.id.txt_CO));
            TextView txttime = ((TextView) convertView.findViewById(R.id.time));
            TextView txtnumber = ((TextView) convertView
                    .findViewById(R.id.txtNumber));
            // 設定元件內容
            txtnumber.setText(libNumber[position] + ". ");
            txttime.setText(libTime[position]);

            if( ! lib_G[position].equals("none") ) {
                if (Double.parseDouble(lib_G[position]) > 100) {
                    txtG.setTextColor(Color.RED); // red
                } else {
                    txtG.setTextColor(Color.BLUE); // blue
                }

                if (Double.parseDouble(lib_B[position]) > 100) {
                    txtB.setTextColor(Color.RED); // red
                } else {
                    txtB.setTextColor(Color.BLUE); // blue
                }

                if (Double.parseDouble(lib_C[position]) > 100) {
                    txtC.setTextColor(Color.RED); // red
                } else {
                    txtC.setTextColor(Color.BLUE); // blue
                }
            }


            txtG.setText(lib_G[position]);
            txtB.setText(lib_B[position]);
            txtC.setText(lib_C[position]);


            // 設定滾動功能(跑馬燈)
            // txttitle.setMovementMethod(ScrollingMovementMethod.getInstance());
            // 設定跑馬燈(取得焦點)
            txtG.setSelected(true);
            txtB.setSelected(true);
            txtC.setSelected(true);

            // 依照類別 選擇圖片
            if (libType[position].contains("學生"))
                imgSelect.setImageResource(imageIds[0]);
            else if (libType[position].contains("專題"))
                imgSelect.setImageResource(imageIds[1]);
            else
                //imgSelect.setImageResource(imageIds[1]);
                imgSelect.setVisibility(View.GONE);
            return convertView;
        }
    }

    private void changeTextColor( String str_color )
    {


    }

    private CompoundButton.OnCheckedChangeListener TBtnListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if( mOutputStream == null ) return;

            if( isChecked ){
                try {

                    // 取得outputstream
                    mOutputStream = mBluetoothSocket.getOutputStream();
                    // 送出訊息
                    String message = "1";
                    mOutputStream.write(message.getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    // 取得outputstream
                    mOutputStream = mBluetoothSocket.getOutputStream();

                    // 送出訊息
                    String message = "0";
                    mOutputStream.write(message.getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void isConnectDevice() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(MainActivity.this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // If bluetooth id no open
        if (!mBluetoothAdapter.isEnabled()) {
            // 發出一個intent去開啟藍芽
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if( mBluetoothAdapter.isEnabled() ) {
            // 取得目前已經配對過的裝置
            //Set<BluetoothDevice> setPairedDevices = mBluetoothAdapter.getBondedDevices();

            // 註冊一個BroadcastReceiver，等等會用來接收搜尋到裝置的消息
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            myBluetoothBroadcastReceiver = mReceiver;
            registerReceiver(myBluetoothBroadcastReceiver, filter);
            mBluetoothAdapter.startDiscovery(); //開始搜尋裝置

        }
    };

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String msg = "";
            switch (v.getId()) {
                case R.id.btnConnectDevice:
                    isConnectDevice();
                    break;
                case R.id.btnDisconnect:
                    DisConnect();
                    msg = "藍芽已關閉連線";
                    break;
//                case R.id.btnSearchDevice:
//                    Intent searchDeviceIntent = new Intent();
//                    searchDeviceIntent.setClass(MainActivity.this, SearchDevice.class);
//                    startActivity(searchDeviceIntent);
//                    break;
//                case R.id.btnOpen:
//                    try {
//
//                        // 取得outputstream
//                        mOutputStream = mBluetoothSocket.getOutputStream();
//                        // 送出訊息
//                        String message = "1";
//                        mOutputStream.write(message.getBytes());
//
//                        msg = "開";
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                case R.id.btnClose:
//                    try {
//                        // 取得outputstream
//                        mOutputStream = mBluetoothSocket.getOutputStream();
//
//                        // 送出訊息
//                        String message = "0";
//                        mOutputStream.write(message.getBytes());
//
//                        msg = "關";
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
            }

        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // 當收尋到裝置時
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                // 取得藍芽裝置這個物件
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // 判斷那個裝置是不是你要連結的裝置，根據藍芽裝置名稱判斷
                if (device.getAddress().equals("98:D3:31:20:3C:F1")) {
                    try {
                        // 一進來一定要停止搜尋
                        mBluetoothAdapter.cancelDiscovery();

                        // 連結到該裝置
                        mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                        mBluetoothSocket.connect();

                        // 取得outputstream 傳送用
                        mOutputStream = mBluetoothSocket.getOutputStream();

                        // 取得outputstream 接收用
                        mInputStream = mBluetoothSocket.getInputStream();

                        // 送出訊息
                        String message = "hello";
                        mOutputStream.write(message.getBytes());

                        beginListenForDate();

                    } catch (IOException e) {

                    }
                }

            }
        }
    };

    private void DisConnect() {
        stopWorker = true;
        try {
            if (mBluetoothAdapter != null && mBluetoothSocket != null ) {
                if (mBluetoothSocket.isConnected()) {
                    mBluetoothSocket.close();
                }
            }

            // unregisterReceiver
            if(myBluetoothBroadcastReceiver != null){
                unregisterReceiver(mReceiver);
                myBluetoothBroadcastReceiver = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  離開時關閉連線
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DisConnect();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.clean:
                db.clearAll();
                upDataListView();
                break;
        }
        return true;
    }

    // 返回 離開 確認訊息
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {// 監控/攔截/遮蔽返回鍵
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("確認視窗")
                    .setMessage("確定要結束應用程式嗎?")
                    .setPositiveButton("確定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    finish();
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub
                                }
                            }).show();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {// 監控攔劫菜單鍵
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

