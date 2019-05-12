package clerk.controllers.dialog;

import clerk.model.Event;
import clerk.model.Room;
import clerk.model.Worker;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class CalendarController {

    SimpleDateFormat formatt = new SimpleDateFormat("HH:mm");
    ObservableList<Interval> intervals;
    ObservableList<Event> events;
    Worker worker = null;
    Room room = null;

    @FXML
    TableView<Interval> tableViewCalendar;
    @FXML
    TableColumn columnTime, columnEventName;
    @FXML
    ComboBox comboBox;
    @FXML
    DatePicker calendarDay;

    public CalendarController() {
    }

    @FXML
    public void initialize(){
        columnTime.setCellValueFactory(new PropertyValueFactory<Interval, String>("timeInterval"));
        columnEventName.setCellValueFactory(new PropertyValueFactory<Interval, String>("eventName"));
        calendarDay.setValue(LocalDate.now());
        calendarDay.valueProperty().addListener(changeListener);
        comboBox.valueProperty().addListener(changeListener);
        tableViewCalendar.setRowFactory(new Callback<TableView<Interval>, TableRow<Interval>>() {
            @Override
            public TableRow<Interval> call(TableView<Interval> param) {
                TableRow<Interval> row = new TableRow<Interval>() {
                    @Override
                    protected void updateItem(Interval item, boolean empty) {
                        // Сначала обязательно сбрасываем стиль.
                        setStyle("");
                        // и только после этого вызываем метод super.updateItem
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setStyle("");
                        } else {
                            String eventName = item.getEventName();
                            if (eventName.equals("")){
                                setStyle("-fx-background-color:#E0F4B0");
                            }else {
                                setStyle("-fx-background-color:#FFC8DB");
                            }
                        }
                    }
                };
                return row;
            }
        });
    }

    ChangeListener changeListener = new ChangeListener<Object>() {
        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            updateTable();
        }
    };

    private void updateTable() {
        intervals.clear();
        int interval = Integer.parseInt(((String)comboBox.getValue()).substring(0, 2));
        int countOfIntervals = 24 * 60 / interval;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendarDay.getValue().getYear());
        calendar.set(Calendar.MONTH, calendarDay.getValue().getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendarDay.getValue().getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        for (int i = 0; i < countOfIntervals; i++) {
            /*calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);*/
            Event eventFinded = null;
            for (int j = 0; j < events.size(); j++) {
                Event event = events.get(j);
                if (event.getStart() > calendar.getTimeInMillis() || event.getEnd() < calendar.getTimeInMillis())
                    continue;

                if (worker != null && event.getWorkers().contains(worker) ||
                                room != null && room.getId() == event.getRoom().getId()){
                    eventFinded = event;
                    break;
                }
            }
            intervals.add(new Interval(eventFinded != null ? eventFinded.getName() : "",
                    calendar.getTimeInMillis(), interval));
            calendar.add(Calendar.MINUTE,  interval);
        }
    }

    public void setModel(ObservableList<Event> events, Object object){
        if (object instanceof Worker)
            worker = (Worker) object;
        else
            room = (Room) object;
        intervals = FXCollections.observableArrayList();
        this.events = events;
        tableViewCalendar.setItems(intervals);
        comboBox.getItems().addAll("15 мин.", "30 мин.", "60 мин.");
        comboBox.setValue("15 мин.");
        updateTable();
    }

    public class Interval{
        String eventName;
        long timeStart;
        long interval;
        String timeInterval;

        public Interval(String eventName, long timeStart, long interval) {
            this.eventName = eventName;
            this.timeStart = timeStart;
            this.interval = interval;
            this.timeInterval = formatt.format(new Date(timeStart)) + " - " +
                    formatt.format(new Date(timeStart + interval * 1000 * 60));
        }

        public String getEventName() {
            return eventName;
        }

        public long getTimeStart() {
            return timeStart;
        }

        public long getInterval() {
            return interval;
        }

        public String getTimeInterval() {
            return timeInterval;
        }
    }
}
