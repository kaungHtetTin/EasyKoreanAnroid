package com.calamus.easykorean.app;


import android.database.sqlite.SQLiteDatabase;


public  class SQLiteHandler {



    public static void deleteRowRromTable(String dbPath,String table, String roll,String rowWhere){

        SQLiteDatabase db= SQLiteDatabase.openDatabase(dbPath,null,SQLiteDatabase.OPEN_READWRITE);

        db.delete(table, roll + "=" +rowWhere, null);

    }

}
