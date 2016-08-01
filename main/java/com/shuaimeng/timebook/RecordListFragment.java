package com.shuaimeng.timebook;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecordListFragment extends ListFragment {
    private RecordDBHelper dbHelper;
    private RecordDBHelper.RecordCursor rCursor;
    private Button button;
    private TextView yearText;
    private TextView monthText;
    private int month;
    private TextView dayText;
    private int day;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("TimeRecorder");

        Calendar c = Calendar.getInstance();
        month = c.get(Calendar.MONTH) + 1;
        day = c.get(Calendar.DATE);

        dbHelper = new RecordDBHelper(getActivity(), c.get(Calendar.YEAR)+"", 2);
        rCursor = dbHelper.queryRecords(month + "-" + day);
        RecordCursorAdapter adapter = new RecordCursorAdapter(getActivity(), rCursor);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_list, parent, false);

        final Calendar c = Calendar.getInstance();
        yearText = (TextView)v.findViewById(R.id.year);
        yearText.setText(c.get(Calendar.YEAR)+"");
        monthText = (TextView)v.findViewById(R.id.month);
        monthText.setText(month+"");
        dayText = (TextView)v.findViewById(R.id.day);
        dayText.setText(day+"");

        button = (Button) v.findViewById(R.id.add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Record r = dbHelper.getLastRecordOfDay(month + "-" + day);
                if(r.getId() == -1) {//说明当天还没有记录
                    Log.i("today", "first");
                    r = new Record();
                    r.setEnd("00:00");
                }
                addInfo(r, "add");
            }
        });

        View date = v.findViewById(R.id.date);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year1, int month1, int day1) {
                                yearText.setText(year1+"");
                                month = month1 + 1;
                                monthText.setText(month+"");
                                day = day1;
                                dayText.setText(day1+"");
                                Log.i("date", month1 +"-"+day1);

                                rCursor = dbHelper.queryRecords(month1 + "-" + day1);
                                RecordCursorAdapter adapter = new RecordCursorAdapter(getActivity(), rCursor);
                                setListAdapter(adapter);
                            }
                        }, c.get(Calendar.YEAR), month-1, day).show();
            }
        });
        return v;
    }

    @Override
    public void onDestroy() {
        rCursor.close();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((RecordCursorAdapter)getListAdapter()).notifyDataSetChanged();
        rCursor = dbHelper.queryRecords(month + "-" + day);
        RecordCursorAdapter adapter = new RecordCursorAdapter(getActivity(), rCursor);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Record r = ((RecordDBHelper.RecordCursor)(getListAdapter().getItem(position))).getRecord();
        addInfo(r, "check");
    }

    private void addInfo(Record r, String flag) {
        Intent intent = new Intent(getActivity(), RecordHandler.class);
        intent.putExtra("year",  yearText.getText());
        intent.putExtra("month",  monthText.getText());
        intent.putExtra("day",  dayText.getText());
        intent.putExtra("record",  r);
        intent.putExtra("flag", flag);
        startActivity(intent);
    }

    private static class RecordCursorAdapter extends CursorAdapter {
        private RecordDBHelper.RecordCursor recordCursor;

        public RecordCursorAdapter(Context context, RecordDBHelper.RecordCursor cursor) {
            super(context, cursor, 0);
            recordCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.fragment_item, parent, false);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Record record = recordCursor.getRecord();

            TextView event = (TextView)view.findViewById(R.id.event);
            event.setText(record.getEvent()+"   ");
            TextView start = (TextView)view.findViewById(R.id.start);
            start.setText(record.getStart()+"   ");
            TextView end = (TextView)view.findViewById(R.id.end);
            end.setText(record.getEnd()+"   ");
            TextView span = (TextView)view.findViewById(R.id.span);
            span.setText(record.getSpan()+"min   ");
            TextView percent = (TextView)view.findViewById(R.id.percent);
            percent.setText(record.getPercent()+"%");
        }
    }
}
