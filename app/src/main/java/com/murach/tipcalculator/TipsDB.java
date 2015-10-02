package com.murach.tipcalculator;

/**
 * Created by Bob Lull on 9/22/2015.
  */

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.Date;
// newer version project 6


public class TipsDB
{
    private SQLiteDatabase db;
    private DBHelper dbHelper;


    // database constants
    public static final String DB_NAME = "tips.db";
    public static final int DB_VERSION = 2;

    // tipTable table constants
    public static final String TIP_TABLE = "tipTable";

    public static final String TIP_ID = "_tipId";
    public static final int TIP_ID_COL = 0;

    public static final String BILL_DATE = "bill_date";
    public static final int BILL_DATE_COL = 1;

    public static final String BILL_AMT = "bill_amount";
    public static final int BILL_AMT_COL = 2;

    public static final String TIP_PERCENT = "tip_percent";
    public static final int TIP_PERCENT_COL = 3;


    //CREATE and DROP TABLE statements

    public static final String CREATE_TIP_TABLE = "CREATE TABLE " + TIP_TABLE + " (" +
            TIP_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BILL_DATE + " INTEGER NOT NULL, " +
            BILL_AMT + " REAL NOT NULL, " +
            TIP_PERCENT + " REAL NOT NULL);";

    public static final String DROP_TIP_TABLE = "DROP TABLE IF EXISTS " + TIP_TABLE;

    private static class DBHelper extends SQLiteOpenHelper
    {
        public DBHelper(Context context, String name, CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_TIP_TABLE);

            //create default entries into tipTable.
            db.execSQL("INSERT INTO " + TIP_TABLE + " VALUES ('1', '" + System.currentTimeMillis() + "', '84.79', '.15');");
            db.execSQL("INSERT INTO " + TIP_TABLE + " VALUES ('2', '" + System.currentTimeMillis() + "', '107.34', '.20');");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL(DROP_TIP_TABLE);
            onCreate(db);
        }
    }



    public TipsDB(Context context)
    {
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);

    }

    private void openWriteableDB()
    {
        db = dbHelper.getWritableDatabase();
    }

    private void openReadableDB()
    {
        db = dbHelper.getReadableDatabase();
    }

    private void closeDB()
    {
        db.close();
    }

    public ArrayList<Tip> getTips()
    {
        openReadableDB();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TIP_TABLE + ";", null);

        ArrayList<Tip> tips = new ArrayList<Tip>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Tip tip = new Tip(cursor.getInt(TIP_ID_COL),
                              cursor.getLong(BILL_DATE_COL),
                              cursor.getFloat(BILL_AMT_COL),
                              cursor.getFloat(TIP_PERCENT_COL));
            tips.add(tip);
            cursor.moveToNext();
        }

 /*       for (Tip tip : tips){
            Log.i(TAG, tip.getDateMillis() + ", " + tip.getId() + ", " + tip.getBillAmount() +
                  ", " + tip.getTipPercent()); */
        closeDB();
        return tips;
    }

    public void saveTip(Tip tip){
     /*   openWriteableDB();
        db.execSQL("INSERT INTO " + TIP_TABLE + " ( VALUES ('" + null + "', '" + tip.getDateMillis() +
        "', '" + tip.getBillAmount() + "', '" + tip.getTipPercent() + "');" );
     */
        ContentValues cv = new ContentValues();
        cv.put(BILL_AMT, tip.getBillAmount());
        cv.put(BILL_DATE, tip.getDateMillis());
        cv.put(TIP_PERCENT, tip.getTipPercent());


        this.openWriteableDB();
        db.insert(TIP_TABLE, null, cv);
        this.closeDB();
    }

    public Tip getLastTipDate(){
        Tip lastTipSaved = new Tip();

        this.openReadableDB();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TIP_TABLE + " WHERE " +
                        TIP_ID + " IN(SELECT MAX(" + TIP_ID + ") FROM " + TIP_TABLE + ")", null);
        cursor.moveToFirst();

        if(cursor == null || cursor.getCount() == 0) {
            this.closeDB();
            return null;
        }
        else{
            try{
                lastTipSaved.setDateMillis(cursor.getLong(BILL_DATE_COL));
                lastTipSaved.setBillAmount(cursor.getFloat(BILL_AMT_COL));
                lastTipSaved.setTipPercent(cursor.getFloat(TIP_PERCENT_COL));
                lastTipSaved.setId(cursor.getInt(TIP_ID_COL));
                closeDB();
                return lastTipSaved;
            }
            catch(Exception e){
                Log.d("com.lull.TipCalculator", "Exception occurred: " + e.toString());
            }
        }
        this.closeDB();
        return null;

    }
    public float setAvgTipPercent(){
        float avgTipPercent = 0.0f;
        openReadableDB();
        Cursor cursor = db.rawQuery("SELECT AVG(" + TIP_PERCENT + ") FROM " + TIP_TABLE, null);
        cursor.moveToFirst();
        if (!(cursor == null)){
            avgTipPercent = cursor.getFloat(0);
            return avgTipPercent;
        }
        else{
            return 0.0f;
        }
    }

}

