package com.hyrglobal.hyrtimesheet;

/**
 * Created by nisha on 4/6/2017.
 */

public class TimeEntry {
    private String mDate;
    private String mHours;
    private String mstatus;

    public TimeEntry(String dateVal, String hoursVal)
    {
        mDate = dateVal;
        mHours = hoursVal;
    }

    public TimeEntry(String dateVal, String hoursVal,String statusVal)
    {
        mDate = dateVal;
        mHours = hoursVal;
        mstatus = statusVal;
    }

    public String getmDate()
    {
        return mDate;
    }

    public String getmHours()
    {
        return mHours;
    }

    public void setmHours(String val)
    {
        mHours = val;
    }

    public String getmStatus()
    {
        return mstatus;
    }

    public void setmStatus(String val)
    {
        mstatus  = val;
    }
}
