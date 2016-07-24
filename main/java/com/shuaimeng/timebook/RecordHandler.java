package com.shuaimeng.timebook;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class RecordHandler extends Activity {

    private TextView today;
    private TextView start;
    private TextView end;
    private EditText editText;
    private Button save;
    private Button cancel;
    private RecordDBHelper dbHelper;
    private String year;
    private String month;
    private String day;
    private Record record;
    private String flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.handler);
        today = (TextView) findViewById(R.id.today);
        start  = (TextView) findViewById(R.id.start);
        end = (TextView) findViewById(R.id.end);
        editText = (EditText) findViewById(R.id.event);
        save = (Button)findViewById(R.id.save);
        cancel = (Button)findViewById(R.id.cancel);

        Intent intent = getIntent();
        year = intent.getStringExtra("year");
        month = intent.getStringExtra("month");
        day = intent.getStringExtra("day");
        flag = intent.getStringExtra("flag");
        record = (Record)intent.getSerializableExtra("record");

        dbHelper = new RecordDBHelper(this, year, 2);
        setElem();
    }

    private void setElem() {
        if("add".equals(flag)) {
            start.setText(record.getEnd());
            SimpleDateFormat df = new SimpleDateFormat("hh:mm");
            end.setText(df.format(new Date()));

            cancel.setVisibility(View.GONE);
        } else {
            start.setText(record.getStart());
            end.setText(record.getEnd());
            editText.setText(record.getEvent());

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbHelper.deleteRecord(record);
                    finish();
                }
            });
        }

        today.setText(year+"-"+month+"-"+day);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime(start);
            }
        });

        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime(end);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                setResult(RESULT_OK, new Intent());
                finish();
            }
        });
    }

    private void setTime(final TextView t) {
        String text = t.getText().toString();
        int[] time = getNumberFromTime(text);
        new TimePickerDialog(RecordHandler.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view,int hourOfDay, int minute) {
                        t.setText(hourOfDay + ":" + minute);
                    }
                }, time[0], time[1],
                false).show();
    }

    private int[] getNumberFromTime(String time) {
        int[] res = new int[2];
        String[] arr = time.split(":");
        res[0] = Integer.parseInt(arr[0]);
        res[1] = Integer.parseInt(arr[1]);

        return res;
    }

    private void saveData() {
        Record r = new Record();
        if("check".equals(flag))
            r.setId(record.getId());
        r.setEvent(editText.getText().toString());
        r.setDate(month+"-"+day);
        r.setStart(start.getText().toString());
        r.setEnd(end.getText().toString());
        int length = timeSpan(start.getText().toString(), end.getText().toString());
        r.setSpan(length);
        float p = length * 100 / (24.0f * 60);
        r.setPercent(String.format("%.2f",p));

        dbHelper.insertRecord(r, flag);
    }

    private int timeSpan(String start, String end) {
        int[] start1 = getNumberFromTime(start);
        int early = start1[0] * 60 + start1[1];
        int[] end1 = getNumberFromTime(end);
        int late = end1[0] * 60 + end1[1];
        return late - early;
    }
}
