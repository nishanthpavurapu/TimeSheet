package com.hyrglobal.hyrtimesheet;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import static android.R.attr.format;

/**
 * Created by nisha on 4/6/2017.
 */

public class NewTimeEntry extends AppCompatActivity{

    private ListView mListView;
    private DatePickerDialog mDatePickerDialog;
    private SimpleDateFormat simpleDateFormat;
    private EditText mDateSelectedEditText;
    private Calendar mCalendar;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TextView totalHoursText;
    private TimeEntryAdapter timeEntryAdapter;
    private int totalCalHours = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_time_entry);

        mListView = (ListView) findViewById(R.id.timeEntryListView);
        mDateSelectedEditText = (EditText) findViewById(R.id.dateSelectedEditText);
        mDateSelectedEditText.setInputType(InputType.TYPE_NULL);
        TextView emptyText = (TextView)findViewById(android.R.id.empty);

        totalHoursText = (TextView) findViewById(R.id.totalHoursText);

        Button temp = (Button) findViewById(R.id.buttonSubmitEntries);
        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateTotalHours();
            }
        });

        mCalendar = Calendar.getInstance();
        mCalendar.setTime(new Date());

        mListView.setEmptyView(emptyText);

        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        mDateSelectedEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating dat picker dialog
                dateSetListener = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mCalendar.set(Calendar.YEAR, year);
                        mCalendar.set(Calendar.MONTH, monthOfYear);
                        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        Date dateRetained = new Date(year,monthOfYear,dayOfMonth);
                        mDateSelectedEditText.setText(simpleDateFormat.format(mCalendar.getTime()));
                        ArrayList<String> daysOfMonth = new ArrayList<String>();
                        if(dayOfMonth<=15) {
                            mCalendar.set(Calendar.DAY_OF_MONTH,mCalendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                            for (int i = 0; i < 15; i++) {
                                daysOfMonth.add(simpleDateFormat.format(mCalendar.getTime()));
                                mCalendar.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        }
                        else
                        {
                            int max = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - 15;
                            mCalendar.set(Calendar.DAY_OF_MONTH,16);
                            for (int i = 0; i < max; i++) {
                                daysOfMonth.add(simpleDateFormat.format(mCalendar.getTime()));
                                mCalendar.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        }
                        updateListView(daysOfMonth);
                    }
                };
                DatePickerDialog dp = new DatePickerDialog(NewTimeEntry.this, dateSetListener, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                dp.show();
            }
        });
        calculateTotalHours();
    }

    private void calculateTotalHours()
    {
        int totalHours = 0;
        try {
            for (int i = 0; i < timeEntryAdapter.getCount(); i++) {
                TimeEntry timeEntry = timeEntryAdapter.getItem(i);
                totalHours = totalHours + Integer.parseInt(timeEntry.getmHours());
            }
            totalHoursText.setText(totalHours + " Hrs");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void updateListView(ArrayList<String> daysOfMonth) {
        final ArrayList<TimeEntry> timeEntries = new ArrayList<TimeEntry>();
        for (String dateSelected:daysOfMonth) {
            timeEntries.add(new TimeEntry(dateSelected,"00"));
        }

        timeEntryAdapter = new TimeEntryAdapter(this, R.layout.list_item_entry,timeEntries);
        mListView.setAdapter(timeEntryAdapter);
        timeEntryAdapter.notifyDataSetChanged();
    }
    class TimeEntryAdapter extends ArrayAdapter<TimeEntry> {

        private ArrayList<TimeEntry> timeEntryList;

        public TimeEntryAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<TimeEntry> timeEntries) {
            super(context, resource, timeEntries);
            this.timeEntryList = new ArrayList<TimeEntry>();
            this.timeEntryList.addAll(timeEntries);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            TimeEntry mCurrentTimeEntry = timeEntryList.get(position);
            View listItemView = convertView;

            if(listItemView == null)
            {
                listItemView  = LayoutInflater.from(getContext()).inflate(R.layout.list_item_entry,parent,false);
                //TextView mDateTextView = (TextView) listItemView.findViewById(R.id.dateTextView);
                EditText mHoursTextView = (EditText) listItemView.findViewById(R.id.hoursEditText);
                mHoursTextView.addTextChangedListener(new MyTextWatcher(listItemView));
            }

            TextView mDateTextView = (TextView) listItemView.findViewById(R.id.dateTextView);
            mDateTextView.setText(mCurrentTimeEntry.getmDate());

            EditText mHoursTextView = (EditText) listItemView.findViewById(R.id.hoursEditText);
            mHoursTextView.setTag(mCurrentTimeEntry);
            if(mHoursTextView != null) {
                mHoursTextView.setText(mCurrentTimeEntry.getmHours());
            }

            return listItemView;
        }
    }

    class MyTextWatcher implements TextWatcher {

        private View view;

        public MyTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            String hourString = s.toString().trim();
            int hours = hourString.equals("") ? 0:Integer.valueOf(hourString);
            EditText mEditText = (EditText) view.findViewById(R.id.hoursEditText);
            TimeEntry timeEntry = (TimeEntry) mEditText.getTag();

            if(timeEntry.getmHours() != (hours+""))
            {
                timeEntry.setmHours(mEditText.getText().toString());
                calculateTotalHours();
            }
        }
    }
}
