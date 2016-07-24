package com.shuaimeng.timebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

/**
 * Created by 201507200439 on 2016/5/22.
 */
public class RecordDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "timeBook";
    private String TABLE;

    public RecordDBHelper(Context context, String tableName, int version) {
        super(context, DB_NAME, null, version);
        this.TABLE = "year" + tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "create table " + TABLE
                + " (_id integer primary key autoincrement, "
                + "start text, "
                + "end text, "
                + "span integer, "
                + "percent real, "
                + "event text, "
                + "date text)";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE);
        onCreate(db);
    }

    public long insertRecord(Record record, String flag) {
        ContentValues cv = new ContentValues();
        cv.put("start", record.getStart().toString());
        cv.put("end", record.getEnd().toString());
        cv.put("date", record.getDate());
        cv.put("event", record.getEvent());
        cv.put("span", record.getSpan());
        cv.put("percent", record.getPercent());

        if("add".equals(flag))
            return getWritableDatabase().insert(TABLE, null, cv);
        else
            return getWritableDatabase().update(TABLE, cv, "_id = ?", new String[] {String.valueOf(record.getId())});
    }

    public int updateRecord(Record record) {
        ContentValues cv = new ContentValues();
        cv.put("start", record.getStart().toString());
        cv.put("end", record.getEnd().toString());
        cv.put("date", record.getDate());
        cv.put("event", record.getEvent());
        cv.put("span", record.getSpan());
        cv.put("percent", record.getPercent());

        return getWritableDatabase().update(TABLE, cv, "_id = ?", new String[] {String.valueOf(record.getId())});
    }

    public void deleteRecord(Record record) {
        getWritableDatabase().delete(TABLE, "_id = ?", new String[] {String.valueOf(record.getId())});
    }

    public RecordCursor queryRecords(String date) {
        Cursor wrapped = getWritableDatabase().query(TABLE, null,  "date = ?", new String[] {date},
                null, null, null);
        return new RecordCursor(wrapped);
    }

    public Record getLastRecordOfDay(String date) {
        Cursor wrapped = getWritableDatabase().query(TABLE, null,  "date = ?", new String[] {date},
                null, null, "_id desc");
        Record record = new Record();
        if(wrapped.moveToFirst()) {
            record.setId(wrapped.getLong(wrapped.getColumnIndex("_id")));
            record.setDate(wrapped.getString(wrapped.getColumnIndex("date")));
            record.setStart(wrapped.getString(wrapped.getColumnIndex("start")));
            record.setEnd(wrapped.getString(wrapped.getColumnIndex("end")));
            record.setEvent(wrapped.getString(wrapped.getColumnIndex("event")));
            record.setPercent(wrapped.getString(wrapped.getColumnIndex("percent")));
            record.setSpan(wrapped.getInt(wrapped.getColumnIndex("span")));
        }

        return record;
    }

    public static class RecordCursor extends CursorWrapper {
        public RecordCursor(Cursor c) {
            super(c);
        }

        public Record getRecord() {
            if(isBeforeFirst() || isAfterLast())
                return null;

            Record record = new Record();
            record.setId(getLong(getColumnIndex("_id")));
            record.setDate(getString(getColumnIndex("date")));
            record.setStart(getString(getColumnIndex("start")));
            record.setEnd(getString(getColumnIndex("end")));
            record.setEvent(getString(getColumnIndex("event")));
            record.setPercent(getString(getColumnIndex("percent")));
            record.setSpan(getInt(getColumnIndex("span")));

            return record;
        }
    }
}
