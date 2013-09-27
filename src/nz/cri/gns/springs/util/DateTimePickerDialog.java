package nz.cri.gns.springs.util;

import java.util.Calendar;
import java.util.Date;

import nz.cri.gns.springs.R;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class DateTimePickerDialog extends Dialog implements View.OnClickListener {
	
	public interface DatePickedListener {
		void datePicked(DateTimePickerDialog dialog, Date date);
	};
	
	private DatePickedListener listener;
	
	public DateTimePickerDialog(Context context, DatePickedListener listener) {
		super(context);
		this.listener = listener;
		setContentView(R.layout.dialog_date);
		((DatePicker)findViewById(R.id.date_picker)).setCalendarViewShown(false);
		((TimePicker)findViewById(R.id.time_picker)).setIs24HourView(true);
		setTitle("Enter the date and time");
	}
	
	@Override
	public void show() {
		super.show();
    	findViewById(R.id.date_ok_button).setOnClickListener(this);
    	findViewById(R.id.date_cancel_button).setOnClickListener(this);
	}
	
	 @Override
     public void onClick(View view) {
		 if (view.getId() == R.id.date_ok_button) {
         	if (listener != null) {
        		listener.datePicked(this, getDate());
        	}
		 }
		 
		 dismiss();
	 }
	
	public void setDate(Calendar date) {
		DatePicker dp = (DatePicker)findViewById(R.id.date_picker);
		dp.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
		
		TimePicker tp = (TimePicker)findViewById(R.id.time_picker);
		tp.setCurrentHour(date.get(Calendar.HOUR_OF_DAY));
		tp.setCurrentMinute(date.get(Calendar.MINUTE));
	}
	
	public Date getDate() {
		DatePicker dp = (DatePicker)findViewById(R.id.date_picker);
		TimePicker tp = (TimePicker)findViewById(R.id.time_picker);
		return  getDate(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), tp.getCurrentHour(), tp.getCurrentMinute(), 0);
	}
	
	public static Date getDate(int year, int month, int day, int hour, int minute, int second) {
		Calendar c = Calendar.getInstance();

		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		
		return c.getTime();
	}

}
