package com.hutchgroup.elog.tracklocations;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GPSSQLiteOpenHelper extends SQLiteOpenHelper {
	public static final String TABLE_GpsLocation= "GpsLocation";

	private static final String DATABASE_NAME = "cbsparts.gps.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_CREATE_GpsLocation = "create table "
			+ TABLE_GpsLocation
			+ "("
			
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,signal text)";


	public GPSSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE_GpsLocation);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
