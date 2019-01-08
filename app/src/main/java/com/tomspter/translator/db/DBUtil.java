package com.tomspter.translator.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class DBUtil {

    public static Boolean queryIfItemExist(NotebookDatabaseHelper dbhelper, String queryString){
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor cursor = db.query("notebook",null,null,null,null,null,null);
        //移动光标到第一个
        if (cursor.moveToFirst()){
            do {
                String s = cursor.getString(cursor.getColumnIndex("input"));
                if (queryString.equals(s)){
                    Log.i("SQLite", "queryIfItemExist:"+s+"-------exist-------");
                    return true;
                }
            } while (cursor.moveToNext());
        }

        //关闭cursor
        cursor.close();

        return false;
    }

    public static void insertValue(NotebookDatabaseHelper dbhelper, ContentValues values){
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        db.insert("notebook",null,values);
        Log.i("SQLite", "insertValue: success!");
    }

    public static void deleteValue(NotebookDatabaseHelper dbhelper,String deleteString){
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        db.delete("notebook","input = ?",new String[]{deleteString});
        Log.i("SQLite", "deleteValue: success!");
    }

}
