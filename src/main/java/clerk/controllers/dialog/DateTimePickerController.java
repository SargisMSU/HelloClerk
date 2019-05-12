package clerk.controllers.dialog;

import javafx.fxml.FXML;
import jfxtras.scene.control.CalendarPicker;

import java.util.Calendar;

public class DateTimePickerController {

    public interface OnDateSetListener{
        void onDateSet(long date, boolean isStart);
    }

    @FXML
    CalendarPicker calendarPicker;

    OnDateSetListener onDateSetListener;
    boolean isStart;

    public void setModel(OnDateSetListener onDateSetListener, long date, boolean isForStartPicker) {
        this.onDateSetListener = onDateSetListener;
        this.isStart = isForStartPicker;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        calendarPicker.setCalendar(cal);
        /*Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 29);
//        calendarPicker.highlightedCalendars().addAll( Calendar.getInstance(), c);
        calendarPicker.disabledCalendars().addAll(Calendar.getInstance(), c);*/

    }

    public void onOkCalendar(){
        onDateSetListener.onDateSet(calendarPicker.getCalendar().getTimeInMillis(), isStart);
        calendarPicker.getScene().getWindow().hide();
    }

    public void onCancellCalendar(){
        calendarPicker.getScene().getWindow().hide();
    }
}
