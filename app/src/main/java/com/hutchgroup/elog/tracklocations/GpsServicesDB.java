package com.hutchgroup.elog.tracklocations;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GpsServicesDB {

	GPSSQLiteOpenHelper helper;
	Context ctx;

	public GpsServicesDB(Context ctx) {
		helper = new GPSSQLiteOpenHelper(ctx);
		this.ctx = ctx;
	}

	public void closeDB() {
		helper.close();
	}

	public void addGpsSignal(String signal) {
		SQLiteDatabase database = null;
		try {

			database = helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("signal", signal);
			database.insert(GPSSQLiteOpenHelper.TABLE_GpsLocation, "_id", values);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				closeDB();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public long removeGpsSignal(String id) {

		SQLiteDatabase database = null;
		long res = -1;
		try {
			database = helper.getWritableDatabase();
			res = database.delete(GPSSQLiteOpenHelper.TABLE_GpsLocation,
					"_id=?", new String[] { id });

		} catch (Exception e) {
			System.out.println("removeGpsSignal");
			e.printStackTrace();
		} finally {
			try {
				closeDB();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	public ArrayList<GpsSignal> getGpsSignalList() {
		SQLiteDatabase database = helper.getWritableDatabase();
		;
		ArrayList<GpsSignal> lstSignal = new ArrayList<GpsSignal>();
		Cursor cursor = null;
		try {
			cursor = database.query(GPSSQLiteOpenHelper.TABLE_GpsLocation, null,

			null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {

				cursor.moveToFirst();
				GpsSignal objBean;
				while (!cursor.isAfterLast()) {
					objBean = new GpsSignal();
					objBean.set_id(cursor.getInt(0));
					objBean.set_gpsSignal(cursor.getString(cursor
							.getColumnIndex("signal")));

					lstSignal.add(objBean);
					cursor.moveToNext();
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			try {
				cursor.close();
				closeDB();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return lstSignal;
	}

}

class GpsSignal {
	private int _id;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String get_gpsSignal() {
		return _gpsSignal;
	}

	public void set_gpsSignal(String _gpsSignal) {
		this._gpsSignal = _gpsSignal;
	}

	private String _gpsSignal;

}
