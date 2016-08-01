package com.shuaimeng.timebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordHandler extends Activity {

    private TextView today;
    private TextView start;
    private TextView end;
    private EditText editText;
    private Button save;
    private Button delete;
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
        delete = (Button)findViewById(R.id.delete);

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
        today.setText(year+"-"+month+"-"+day);

        if("add".equals(flag)) {
            start.setText(record.getEnd());
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            end.setText(df.format(new Date()));

            delete.setVisibility(View.GONE);
        } else {
            start.setText(record.getStart());
            end.setText(record.getEnd());
            editText.setText(record.getEvent());

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbHelper.deleteRecord(record);
                    finish();
                }
            });
        }

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
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.show();
        dialog.getWindow().setContentView(View.inflate(this, R.layout.time_picker, null));

        int[] time = getNumberFromTime(t.getText().toString());
        Log.i("val",t.getText().toString());
        final NumberPicker hourPicker = (NumberPicker) dialog.findViewById(R.id.hourPicker);
        setPicker(hourPicker, 0, 23, time[0]);
        final NumberPicker minutePicker = (NumberPicker) dialog.findViewById(R.id.minutePicker);
        setPicker(minutePicker, 0, 59, time[1]);

        Button ok = (Button) dialog.findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String res = formatTime(hourPicker.getValue()) + ":" +
                        formatTime(minutePicker.getValue());
                t.setText(res);
                dialog.dismiss();
            }
        });

        Button cancel = (Button) dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
    }

    private void setPicker(NumberPicker picker, int min, int max, int init) {
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setValue(init);
        picker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                String tmpStr = String.valueOf(value);
                if (value < 10) {
                    tmpStr = "0" + tmpStr;
                }
                return tmpStr;
            }
        });
    }

    private int[] getNumberFromTime(String time) {
        int[] res = new int[2];
        String[] arr = time.split(":");
        res[0] = Integer.parseInt(arr[0]);
        res[1] = Integer.parseInt(arr[1]);

        return res;
    }

    private String formatTime(int value) {
        if(value < 10)
            return "0" + value;
        return value + "";
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
