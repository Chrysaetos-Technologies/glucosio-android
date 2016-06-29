/*
 * Copyright (C) 2016 Glucosio Foundation
 *
 * This file is part of Glucosio.
 *
 * Glucosio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Glucosio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Glucosio.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.glucosio.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.glucosio.android.R;
import org.glucosio.android.presenter.AddKetonePresenter;
import org.glucosio.android.tools.AnimationTools;
import org.glucosio.android.tools.FormatDateTime;

import java.text.DecimalFormat;
import java.util.Calendar;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddKetoneActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private AddKetonePresenter presenter;
    private FloatingActionButton doneFAB;
    private TextView addTimeTextView;
    private TextView addDateTextView;
    private TextView readingTextView;
    private int pagerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ketone);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(2);
        }

        Bundle b = getIntent().getExtras();
        if (b!=null) {
            pagerPosition = b.getInt("pager");
        }

        presenter = new AddKetonePresenter(this);
        presenter.getCurrentTime();

        doneFAB = (FloatingActionButton) findViewById(R.id.done_fab);
        addTimeTextView = (TextView) findViewById(R.id.dialog_add_time);
        addDateTextView = (TextView) findViewById(R.id.dialog_add_date);
        readingTextView = (TextView) findViewById(R.id.ketone_add_value);

        FormatDateTime formatDateTime = new FormatDateTime(getApplicationContext());
        addDateTextView.setText(formatDateTime.getCurrentDate());
        addTimeTextView.setText(formatDateTime.getCurrentTime());
        addDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddKetoneActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
                dpd.setMaxDate(now);
            }
        });

        addTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                if (android.text.format.DateFormat.is24HourFormat(getApplicationContext())) {
                    TimePickerDialog tpd = TimePickerDialog.newInstance(AddKetoneActivity.this, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
                    tpd.show(getFragmentManager(), "Timepickerdialog");
                } else {
                    TimePickerDialog tpd = TimePickerDialog.newInstance(AddKetoneActivity.this, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
                    tpd.show(getFragmentManager(), "Timepickerdialog");
                }
            }
        });
        doneFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogOnAddButtonPressed();
            }
        });
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            doneFAB.post(new Runnable() {
                @Override
                public void run() {
                    doneFAB.setVisibility(View.INVISIBLE);
                    new AnimationTools().startCircularReveal(doneFAB);
                }
            });
        }
    }

    private void dialogOnAddButtonPressed() {
        presenter.dialogOnAddButtonPressed(addTimeTextView.getText().toString(),
                addDateTextView.getText().toString(), readingTextView.getText().toString().trim());
    }

    public void showErrorMessage() {
        Toast.makeText(getApplicationContext(), getString(R.string.dialog_error2), Toast.LENGTH_SHORT).show();
    }

    public void finishActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        // Pass pager position to open it again later
        Bundle b = new Bundle();
        b.putInt("pager", pagerPosition);
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int seconds) {
        TextView addTime = (TextView) findViewById(R.id.dialog_add_time);
        DecimalFormat df = new DecimalFormat("00");

        presenter.setReadingHour(df.format(hourOfDay));
        presenter.setReadingMinute(df.format(minute));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        FormatDateTime formatDateTime = new FormatDateTime(getApplicationContext());
        addTime.setText(formatDateTime.getTime(cal));
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        TextView addDate = (TextView) findViewById(R.id.dialog_add_date);
        DecimalFormat df = new DecimalFormat("00");

        presenter.setReadingYear(year + "");
        presenter.setReadingMonth(df.format(monthOfYear + 1));
        presenter.setReadingDay(df.format(dayOfMonth));

        String date = +dayOfMonth + "/" + presenter.getReadingMonth() + "/" + presenter.getReadingYear();
        addDate.setText(date);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
