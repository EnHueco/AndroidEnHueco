package com.diegoalejogm.enhueco.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Diego on 10/17/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{

    // Logcat tag
    private static final String LOG = "DatabaseHelper";
    // Database version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String database_name = "enHueco";

    // Table Names
    private static final String TABLE_USER = "users";
    private static final String TABLE_SCHEDULE = "schedule";
    private static final String TABLE_EVENT = "event";

    private static final String TABLE_USER_SCHEDULE = "user_schedule";
    private static final String TABLE_SCHEDULE_EVENT = "schedule_event";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";



    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
