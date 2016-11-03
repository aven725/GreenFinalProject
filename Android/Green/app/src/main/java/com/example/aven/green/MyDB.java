package com.example.aven.green;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Aven on 2016/1/9.
 */
public class MyDB {
    public SQLiteDatabase db=null; // 資料庫類別
    private final static String	DATABASE_NAME= "db1.db";// 資料庫名稱
    private final static String	TABLE_NAME="table01"; // 資料表名稱
    private final static String	_ID	= "_id"; // 資料表欄位/
    private final static String	D_G = "data_g";
    private final static String	D_B = "data_b";
    private final static String	D_CO = "data_co";
    private final static String	TIME = "time";

    /* 建立資料表的欄位 */
    private final static String	CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + _ID
            + " INTEGER PRIMARY KEY," + D_G + " TEXT,"+ D_B + " TEXT,"+ D_CO + " TEXT,"+ TIME + " DATETIME)";

    private Context mCtx = null;
    public MyDB(Context ctx){ // 建構式
        this.mCtx = ctx;      // 傳入 建立物件的 Activity
    }

    public void open() throws SQLException { // 開啟已經存在的資料庫
        db = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        try	{
            db.execSQL(CREATE_TABLE);// 建立資料表
        }catch (Exception e) {
        }
    }

    public void close() {  // 關閉資料庫
        db.close();
    }

//	public Cursor getAll() { // 查詢所有資料，取出所有的欄位
//	    return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
//	}

    public Cursor getAll() { // 查詢所有資料，只取出三個欄位，取出後100筆資料
        return db.query(TABLE_NAME,
                new String[] {_ID, D_G, D_B,D_CO,TIME},
                null, null, null, null, _ID+" DESC","100");
    }

    public Cursor get(long rowId) throws SQLException { // 查詢指定 ID 的資料，只取出三個欄位
        Cursor mCursor = db.query(TABLE_NAME,
                new String[] {_ID, D_G, D_B,D_CO,TIME},
                _ID +"=" + rowId, null, null, null, null,null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public long append(String data_g,String data_b,String data_co) { // 新增一筆資料
        ContentValues args = new ContentValues();
        args.put(D_G, data_g);
        args.put(D_B, data_b);
        args.put(D_CO, data_co);
        return db.insert(TABLE_NAME, null, args);
    }

    public void append2(String data_g,String data_b,String data_co) { // 新增一筆資料
        db.execSQL("INSERT INTO "+TABLE_NAME+"("+D_G+","+D_B+","+D_CO+","+TIME+")"+" VALUES ("+"'"+data_g+"',"+"'"+data_b+"',"+"'"+data_co+"',"+"datetime('now','+8 hour') )");
    }

    public boolean delete(long rowId) {  //刪除指定的資料
        return db.delete(TABLE_NAME, _ID + "=" + rowId, null) > 0;
    }

//    public boolean update(long rowId, String name,int price) { // 更新指定的資料
//        ContentValues args = new ContentValues();
//        args.put(NAME, name);
//        args.put(PRICE, price);
//        return db.update(TABLE_NAME, args, _ID + "=" + rowId, null) > 0;
//    }

    public void clearAll(){ // 刪除所有資料
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.execSQL("VACUUM");
    }
}