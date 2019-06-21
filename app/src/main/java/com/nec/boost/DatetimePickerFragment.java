package com.nec.boost;

import android.app.TimePickerDialog;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;

public class DatetimePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    /**日期时间选择结果的代理*/
    public DatetimePickerResultInterface datetimePickerResult = null;
    /**日期选择还是时间选择，默认为True*/
    public boolean datePickerMode = true;
    private Calendar mCalendar = Calendar.getInstance(Locale.CHINA);

    public void setCalendar(Calendar calendar) {
        this.mCalendar = calendar;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(datePickerMode) {
            int year = mCalendar.get(Calendar.YEAR);
            int month = mCalendar.get(Calendar.MONTH);
            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        } else {
            int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = mCalendar.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour,minute,true);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker,int year , int month, int day){
        if(datetimePickerResult != null) {
            mCalendar.set(year, month, day);
            datetimePickerResult.processDatetimePickerResult(mCalendar);
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        if(datetimePickerResult != null) {
            mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), i, i1);
            datetimePickerResult.processDatetimePickerResult(mCalendar);
        }
    }

    /**
     * 日期或时间选择器的结果处理接口
     */
    public interface DatetimePickerResultInterface {
        /**
         * 日期或时间选择器的结果处理
         */
        void processDatetimePickerResult(Calendar cal);
    }

}