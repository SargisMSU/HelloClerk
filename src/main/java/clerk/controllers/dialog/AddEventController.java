package clerk.controllers.dialog;

import clerk.controllers.Controller;
import clerk.model.Room;
import clerk.utils.DatabaseUtils;
import clerk.dialog.DateTimePickerDialog;
import clerk.model.Departament;
import clerk.model.Event;
import clerk.model.Worker;
import clerk.utils.SenderMailTLS;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.prefs.Preferences;

public class AddEventController{

    ObservableList<Event> events;
    ObservableList<Worker> workers;
    ObservableList<Room> rooms;
    ObservableList<Departament> departaments;
    Event event;
    DateFormat format;
    Controller.OnRefreshListener onRefreshListener;
    Semaphore semaphore;

    boolean isChanged = false;

    @FXML
    Button btnOk, btnEnd, btnStart, btnDelete, btnCancell;
    @FXML
    TextField nameTextField, noteTextField;
    @FXML
    CheckComboBox<Object> comboBoxWorkers;
    @FXML
    ComboBox<Room> comboBoxRoom;
    @FXML
    ComboBox<Event.Priority>  comboBoxPriority;
    @FXML
    HBox hBox;

    boolean isSettedModel = false;
    private HashMap<String, Long> sentMessagehashSet;
    private boolean isDataChanged = false;

    @FXML
    public void initialize() {
        nameTextField.textProperty().addListener(changeListener);
        btnStart.textProperty().addListener(changeListener);
        btnEnd.textProperty().addListener(changeListener);
        comboBoxRoom.valueProperty().addListener(changeListener);
        format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        comboBoxPriority.getItems().addAll(Event.Priority.LOW, Event.Priority.NORMAL, Event.Priority.HIGH);
        comboBoxPriority.setValue(Event.Priority.NORMAL);
        comboBoxWorkers.getCheckModel().getCheckedItems().addListener(new ListChangeListener<Object>() {
            @Override
            public void onChanged(Change<?> c) {
                if (isSettedModel) {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            wasAdded();
                        } else if (c.wasRemoved()) {
                            wasRemoved();
                        }
                    }
                }
            }
        });
    }

    public void setModel(Controller.OnRefreshListener onRefreshListener, ObservableList<Departament> departaments,
                         ObservableList<Worker> workers, ObservableList<Event> events,
                         ObservableList<Room> rooms, Event event, Semaphore semaphore,
                         HashMap<String, Long> sentMessagehashSet, boolean isNecessary) {
        this.sentMessagehashSet = sentMessagehashSet;
        this.onRefreshListener = onRefreshListener;
        this.rooms = rooms;
        this.workers = workers;
        this.departaments = departaments;
        this.events = events;
        this.semaphore = semaphore;
        this.event = event != null ? event : new Event();
        comboBoxRoom.setItems(rooms);
        HashMap<String, HashSet<Worker>> groupsOfWorkers = Worker.getGroupsOfWorkers(workers);
        for (Map.Entry<String, HashSet<Worker>> stringHashSetEntry : groupsOfWorkers.entrySet()) {
            comboBoxWorkers.getItems().add("Все сотрудники отдела '" + stringHashSetEntry.getKey() + "'");
            comboBoxWorkers.getItems().addAll(stringHashSetEntry.getValue());
        }

        if (isNecessary){
            btnCancell.setDisable(true);
        }

        try {
            semaphore.acquire();
            if (event != null) {
                nameTextField.setText(event.getName());
                setWorkersToComboBox(comboBoxWorkers, event);
                comboBoxPriority.setValue(event.getPriority());
                comboBoxRoom.setValue(event.getRoom());
                noteTextField.setText(event.getNote() == null ? "" : event.getNote());
                btnOk.setText("Ок");
            } else {
                event = new Event();
                hBox.getChildren().remove(btnDelete.getParent());
            }
            btnStart.setText(format.format(event.getStart()));
            btnEnd.setText(format.format(event.getEnd()));
            semaphore.release();
            isSettedModel = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setWorkersToComboBox(CheckComboBox<Object> comboBoxWorkers, Event event) {
        for (int i = 0; i < event.getWorkers().size(); i++) {
            int k = -1;
            for (int j = 0; j < comboBoxWorkers.getItems().size(); j++) {
                if (comboBoxWorkers.getItems().get(j) instanceof Worker &&
                        (comboBoxWorkers.getItems().get(j)).equals(event.getWorkers().get(i))) {
                    k = j;
                    break;
                }
            }
            if (k != -1) {
                comboBoxWorkers.getCheckModel().check(k);
            }

        }
        int lastIndexOfDep = -1;
        boolean allPrevsSelected = false;
        for (int i = 0; i < comboBoxWorkers.getItems().size(); i++) {
            if (!(comboBoxWorkers.getItems().get(i) instanceof Worker)){
                if (lastIndexOfDep != -1 && allPrevsSelected){
                    comboBoxWorkers.getCheckModel().check(lastIndexOfDep);
                }
                lastIndexOfDep = i;
                allPrevsSelected = true;
            }else {
                if (!comboBoxWorkers.getCheckModel().isChecked(i)){
                    allPrevsSelected = false;
                }
            }
        }
        if (lastIndexOfDep != -1 && allPrevsSelected){
            comboBoxWorkers.getCheckModel().check(lastIndexOfDep);
        }
    }

    public void wasAdded() {
        System.out.println("wasAdded");
        isSettedModel = false;
        int lastIndexOfDep;
        boolean allPrevsSelected;
        boolean isDepSelected;
        int k = 0;
        do {
            lastIndexOfDep = k++;
            isDepSelected = comboBoxWorkers.getCheckModel().isChecked(lastIndexOfDep);
            allPrevsSelected = true;
            if (isDepSelected) {
                do {
                    comboBoxWorkers.getCheckModel().check(k++);
                }while (comboBoxWorkers.getItems().size() != k && (comboBoxWorkers.getItems().get(k) instanceof Worker));
            }else {
                do {
                    if (!comboBoxWorkers.getCheckModel().isChecked(k++)){
                        allPrevsSelected = false;
                    }
                } while (comboBoxWorkers.getItems().size() != k && (comboBoxWorkers.getItems().get(k) instanceof Worker));
                if (allPrevsSelected){
                    comboBoxWorkers.getCheckModel().check(lastIndexOfDep);
                }
            }
        }while (comboBoxWorkers.getItems().size() != k);
        isSettedModel = true;
    }

    public void wasRemoved() {
        System.out.println("wasRemoved");
        isSettedModel = false;
        int lastIndexOfDep;
        boolean allPrevsSelected;
        boolean isDepSelected;
        int k = 0;
        do {
            lastIndexOfDep = k++;
            isDepSelected = comboBoxWorkers.getCheckModel().isChecked(lastIndexOfDep);
            allPrevsSelected = true;
            if (isDepSelected) {
                int countOfChecked = 0;
                int countOfUnChecked = 0;
                do {
                    if (comboBoxWorkers.getCheckModel().isChecked(k++)){
                        countOfChecked++;
                    }else {
                        countOfUnChecked++;
                    }
                }while (comboBoxWorkers.getItems().size() != k && (comboBoxWorkers.getItems().get(k) instanceof Worker));
                if (countOfUnChecked == 1){
                    comboBoxWorkers.getCheckModel().clearCheck(lastIndexOfDep);
                }
            }else {
                do {
                    if (!comboBoxWorkers.getCheckModel().isChecked(k++)){
                        allPrevsSelected = false;
                    }
                } while (comboBoxWorkers.getItems().size() != k && (comboBoxWorkers.getItems().get(k) instanceof Worker));
                if (allPrevsSelected){
                    for (int i = lastIndexOfDep + 1; i < k; i++) {
                        comboBoxWorkers.getCheckModel().clearCheck(i);
                    }
                }
            }
        }while (comboBoxWorkers.getItems().size() != k);
        isSettedModel = true;
    }

    ChangeListener<Object> changeListener = new ChangeListener<Object>() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            isChanged = true;
            if (nameTextField.getText().length() == 0 || btnEnd.getText().equals("Установить время.")||
                    btnStart.getText().equals("Установить время.") || comboBoxRoom.getValue() == null){
                btnOk.setDisable(true);
            }else {
                btnOk.setDisable(false);
            }
        }
    };

    public void handleClickAdd() throws ParseException {
        if (isChanged) {
            String name = nameTextField.getText();
            String note = noteTextField.getText();
            /*if (note == null){
                note = "";
            }*/
            long dateStart = format.parse(btnStart.getText()).getTime();
            long dateEnd = format.parse(btnEnd.getText()).getTime();
            Room room = comboBoxRoom.getValue();
            Event.Priority priority = comboBoxPriority.getValue();
            ObservableList<Integer> checkedIndices = comboBoxWorkers.getCheckModel().getCheckedIndices();
            ArrayList<Worker> newSelectedWorkers = new ArrayList<>();
            for (int i = 0; i < checkedIndices.size(); i++) {
                if (comboBoxWorkers.getItems().get(checkedIndices.get(i)) instanceof Worker)
                    newSelectedWorkers.add((Worker) comboBoxWorkers.getItems().get(checkedIndices.get(i)));
            }
            try {
                semaphore.acquire();
                if (event.getName() == null){
                    event = new Event(room, name, dateStart, dateEnd, newSelectedWorkers, priority, note);
                    if (verifyEvent(events, event)) {
                        DatabaseUtils.insertEvent(event);
                        events.add(event);
                    }else {
                        semaphore.release();
                        return;
                    }
                }else {
                    event.setName(name);
                    event.setNote(note);
                    event.setRoom(room);
                    event.setStart(dateStart);
                    event.setEnd(dateEnd);
                    event.setWorkers(newSelectedWorkers);
                    event.setPriority(priority);
                    if (verifyEvent(events, event)) {
                        /*if (isDataChanged){
                            sendMessageAboutChanges(event);
                        }*/
                        DatabaseUtils.updateEvent(event);
                    }else {
                        semaphore.release();
                        return;
                    }
                }
                semaphore.release();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_EVENT);
        btnOk.getScene().getWindow().hide();
    }

    private boolean verifyEvent(ObservableList<Event> events, Event event){
        Preferences prefs = Preferences.userRoot().node("clerk");
        Boolean autoConflict = prefs.getBoolean("auto", false);

        boolean verified = true;
        if (event.getWorkers().size() > event.getRoom().getCapacity()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Количество участников: " + event.getWorkers().size() +
                    "\n Вместимость комнаты: " + event.getRoom().getCapacity());
            alert.showAndWait();
            return false;
        }
        for (int i = 0; i < events.size(); i++) {
            Event eventTemp = events.get(i);
            if (!eventTemp.equals(event)){
                if (eventTemp.getStart() >= event.getStart() && eventTemp.getStart() < event.getEnd() + 10*60*1000  ||
                    event.getStart() >= eventTemp.getStart() && event.getStart() < eventTemp.getEnd() + 10*60*1000 ) {
                    if (event.getRoom().equals(eventTemp.getRoom())) {
                        System.out.println("Error: " + event.getRoom().getName() + " занята.");
                        Room roomOld, roomTempOld;
                        long startOld, startTempOld;
                        startOld = event.getStart();
                        roomOld = event.getRoom();
                        startTempOld = eventTemp.getStart();
                        roomTempOld = eventTemp.getRoom();
                        if (!autoConflict) {
                            popupWrongRoom(events, event, eventTemp, rooms);
                            if (!roomOld.equals(event.getRoom()) || startOld != event.getStart() ||
                                    !roomTempOld.equals(eventTemp.getRoom()) || startTempOld != eventTemp.getStart()) {
                                verified = true;
                            } else {
                                return false;
                            }
                        } else {
                            return autoSolveRoomConflict(events, event, eventTemp, rooms);
                        }
                    }
                }

                if (eventTemp.getStart() >= event.getStart() && eventTemp.getStart() < event.getEnd() + 10*60*1000 ||
                        event.getStart() >= eventTemp.getStart() && event.getStart() < eventTemp.getEnd() + 10*60*1000){
                    ArrayList<Worker> retainWorkers = (ArrayList<Worker>) event.getWorkers().clone();
                    retainWorkers.retainAll(eventTemp.getWorkers());
                    if (!retainWorkers.isEmpty()){
                        for (int j = 0; j < retainWorkers.size(); j++) {
                            verified = true;
                            System.out.println("error " + retainWorkers.get(j).toString());
                            long startOld = event.getStart();
                            int workersSizeOld = event.getWorkers().size();
                            long startTempOld = eventTemp.getStart();
                            int workersSizeTempOld = eventTemp.getWorkers().size();
                            if (!autoConflict) {
                                popupWrongWorkers(events, event, eventTemp, retainWorkers.get(j));
                            }else {
                                autoSolveWorkersConflict(events, event, eventTemp, retainWorkers);
                                return true;
                            }
                            if(workersSizeOld != event.getWorkers().size() || startOld != event.getStart() ||
                                workersSizeTempOld != eventTemp.getWorkers().size() || startTempOld != eventTemp.getStart()){
                                verified = true;
                                if (workersSizeOld != event.getWorkers().size()) {
                                    setWorkersToComboBox(comboBoxWorkers, event);
                                }
                            }else {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return verified;
    }

    private boolean autoSolveWorkersConflict(ObservableList<Event> events, Event event, Event eventTemp, ArrayList<Worker> retainWorkers) {
        if (event.getPriority() == Event.Priority.HIGH && eventTemp.getPriority() == Event.Priority.LOW){
            showInformationAlert("Так как у события \"" + event.getName() +
                    "\" уровень важности - \"" + Event.Priority.HIGH.toString()+"\", a у события \"" + eventTemp.getName() +
                    "\" уровень важности - \"" + Event.Priority.LOW.toString()+"\", было решено, что " +
                    shift(eventTemp, event, events, rooms));
            DatabaseUtils.updateEvent(eventTemp);
        }else if(eventTemp.getPriority() == Event.Priority.HIGH && event.getPriority() == Event.Priority.LOW){
            showInformationAlert("Так как у события \"" + eventTemp.getName() +
                    "\" уровень важности - \"" + Event.Priority.HIGH.toString()+"\", a у события \"" + event.getName() +
                    "\" уровень важности - \"" + Event.Priority.LOW.toString()+"\", было решено, что " +
                    shift(event, eventTemp, events, rooms));
        }else if (event.isInvolvedDirector() && !eventTemp.isInvolvedDirector()) {
            showInformationAlert("Так как директор фирмы один из участников события \"" +
                    eventTemp.getName() + "\", было решено, что " + shift(eventTemp, event, events, rooms));
            DatabaseUtils.updateEvent(eventTemp);
        }else if (eventTemp.isInvolvedDirector() && !event.isInvolvedDirector()){
            showInformationAlert("Так как директор фирмы один из участников события \"" +
                    eventTemp.getName() + "\", было решено, что " + shift(event, eventTemp, events, rooms));
        }
        //если количесво занятых сотрудников < 20% кол-ва участников второго события
        // и в число занятых не входят директор или руководитель отдела
        else if (retainWorkers.stream().filter(worker -> worker.getPosition().getCode() < 6).count() == retainWorkers.size()) {
            if (retainWorkers.size() * 5 < eventTemp.getWorkers().size()) {
                eventTemp.getWorkers().removeAll(retainWorkers);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < retainWorkers.size(); i++) {
                    stringBuilder.append(retainWorkers.get(i));
                    if (i != retainWorkers.size() -1)
                        stringBuilder.append(", ");
                }
                showInformationAlert("Из списка участников события \"" + eventTemp.getName()
                        + "\" удалены следующие сотрудники:\n" + stringBuilder.toString());
                DatabaseUtils.updateEvent(eventTemp);
            }else if (retainWorkers.size() * 5 < event.getWorkers().size()) {
                event.getWorkers().removeAll(retainWorkers);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < retainWorkers.size(); i++) {
                    stringBuilder.append(retainWorkers.get(i));
                    if (i != retainWorkers.size() -1)
                        stringBuilder.append(", ");
                }
                showInformationAlert("Из списка участников события \"" + event.getName()
                        + "\" удалены следующие сотрудники:\n" + stringBuilder.toString());
            }
        }else if (event.getWeight() > eventTemp.getWeight()){
            showInformationAlert("Так как общий вес события \"" +
                    event.getName() + "\" больше общего веса события \""+ eventTemp.getName() +
                    "\", было решено, что " + shift(eventTemp, event, events, rooms));
            DatabaseUtils.updateEvent(eventTemp);
        }else if (event.getWeight() < eventTemp.getWeight()){
            showInformationAlert("Так как общий вес события \"" +
                    eventTemp.getName() + "\" больше общего веса события \""+ event.getName() +
                    "\", было решено, что " + shift(event, eventTemp, events, rooms));
        }else {
            showErrorAlert("Невозможно автоматический разрешить конфликт!");
            return false;
        }
        return true;
    }

    private boolean autoSolveRoomConflict(ObservableList<Event> events, Event event, Event eventTemp, ObservableList<Room> rooms) {
        if (event.getPriority() == Event.Priority.HIGH && eventTemp.getPriority() == Event.Priority.LOW){
            showInformationAlert("На основе того, что у события \"" + event.getName() +
                    "\" уровень важности \"" + Event.Priority.HIGH.toString()+"\", a у события \"" + eventTemp.getName() +
                    "\" уровень важности \"" + Event.Priority.LOW.toString()+"\", было решено, что " +
                    shift(eventTemp, event, events, rooms));
            DatabaseUtils.updateEvent(eventTemp);
        }else if(eventTemp.getPriority() == Event.Priority.HIGH && event.getPriority() == Event.Priority.LOW){
            showInformationAlert("На основе того, что у события \"" + eventTemp.getName() +
                    "\" уровень важности \"" + Event.Priority.HIGH.toString()+"\", a у события \"" + event.getName() +
                    "\" уровень важности \"" + Event.Priority.LOW.toString()+"\", было решено, что " +
                    shift(event, eventTemp, events, rooms));
        }else if (event.isInvolvedDirector() && !eventTemp.isInvolvedDirector()) {
            showInformationAlert("На основе того, что директор фирмы один из участников события \"" +
                    event.getName() + "\", было решено, что " + shift(eventTemp, event, events, rooms));
            DatabaseUtils.updateEvent(eventTemp);
        }else if (eventTemp.isInvolvedDirector() && !event.isInvolvedDirector()){
            showInformationAlert("На основе того, что директор фирмы один из участников события \"" +
                    eventTemp.getName() + "\", было решено, что " + shift(event, eventTemp, events, rooms));
        }else if (event.getWeight() > eventTemp.getWeight()){
            showInformationAlert("На основе того, что общий вес события \"" +
                    event.getName() + "\" больше общего веса события \""+ eventTemp.getName() +
                    "\", было решено, что " + shift(eventTemp, event, events, rooms));
            DatabaseUtils.updateEvent(eventTemp);
        }else if (event.getWeight() < eventTemp.getWeight()){
            showInformationAlert("На основе того, что общий вес события \"" +
                    eventTemp.getName() + "\" больше общего веса события \""+ event.getName() +
                    "\", было решено, что " + shift(event, eventTemp, events, rooms));
        }else {
            showErrorAlert("Невозможно автоматический разрешить конфликт!");
            return false;
        }
        return true;
    }

    private void showErrorAlert(String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showInformationAlert(String content){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Автоматическое разрешение конфликтов");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private String shift(Event event, Event compareEvent, ObservableList<Event> events, ObservableList<Room> rooms) {
        //если есть свободная комната, то меняем комнату, если нет, то - время начала/конца
        boolean isRoomChanged = false;
        long duration = event.getEnd() - event.getStart();
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).busyWith(events, event.getStart(), event.getEnd()) == null &&
                    rooms.get(i).getCapacity() >= event.getWorkers().size()){
                event.setRoom(rooms.get(i));
                isRoomChanged = true;
                return "событие \"" + event.getName() + "\" будет проводиться в " + rooms.get(i).getName();
            }
        }
        if (!isRoomChanged){
            long compareDate = compareEvent.getEnd() + 15 * 60 * 1000;
            while (true) {
                Event busyWith = event.getRoom().busyWith(events, compareDate, compareDate + duration);
                if (busyWith == null) {
                    event.changeStart(compareEvent.getEnd() + 15 * 60 * 1000);
                    return "событие \"" + event.getName() + "\" будет проводиться в " +
                            format.format(new Date(compareEvent.getEnd() + 15 * 60 * 1000));
                } else {
                    compareDate = busyWith.getEnd() + 15 * 60 * 1000;
                }
            }
        }
        return "";
    }

    public void popupWrongRoom(ObservableList<Event> events, Event event1, Event event2, ObservableList<Room> rooms) {
        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);

        Label conflictLabel = new Label(event1.getName() + ": " + format.format(new Date(event1.getStart()))
                + " - " + format.format(new Date(event1.getEnd())) + "\n" +
                event2.getName() + ": " + format.format(new Date(event2.getStart()))
                + " - " + format.format(new Date(event2.getEnd())) );
        conflictLabel.setFont(Font.font(null, FontWeight.BOLD, 15));

        final Stage dialog = new Stage();
        dialog.setTitle("Конфликт: комната(" + event1.getRoom().getName() + ") занята");
        Button otherRoom1 = new Button("Выбрать другую комнату для события \'" + event1.getName() + "\'");
        Button otherRoom2 = new Button("Выбрать другую комнату для события \'" + event2.getName() + "\'");
        Button event1AfterEvent2 = new Button("Начать событие \'" + event1.getName() +
                "\' после события \'" + event2.getName() + "\'");
        Button event2AfterEvent1 = new Button("Начать событие \'" + event2.getName() +
                "\' после события \'" + event1.getName() + "\'");
        Button  byHimself = new Button("Разрешить конфликт вручную");

        Label displayLabel = new Label("Как разрешить конфликт ?");
        displayLabel.setFont(Font.font(null,  14));

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner((Stage) btnOk.getScene().getWindow());

        HBox dialogHbox = new HBox(20);
        dialogHbox.setAlignment(Pos.CENTER);

        VBox dialogVbox1 = new VBox(20);
        dialogVbox1.setAlignment(Pos.CENTER);

        dialogHbox.getChildren().add(displayLabel);
        dialogVbox1.getChildren().add(otherRoom1);
        dialogVbox1.getChildren().add(otherRoom2);
        dialogVbox1.getChildren().add(event1AfterEvent2);
        dialogVbox1.getChildren().add(event2AfterEvent1);
        dialogVbox1.getChildren().add(byHimself);

        ObservableList<Room> comboBoxEv1 = FXCollections.observableArrayList();
        comboBoxEv1.addAll(rooms);
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getStart() >= event1.getStart() && events.get(i).getStart() < event1.getEnd() ||
                    event1.getStart() >= events.get(i).getStart() && event1.getStart() < events.get(i).getEnd()){
                comboBoxEv1.remove(events.get(i).getRoom());
            }
        }
        if (comboBoxEv1.size() == 0){
            otherRoom1.setDisable(true);
        }

        ObservableList<Room> comboBoxEv2 = FXCollections.observableArrayList();
        comboBoxEv2.addAll(rooms);
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getStart() >= event2.getStart() && events.get(i).getStart() < event2.getEnd() ||
                    event2.getStart() >= events.get(i).getStart() && event2.getStart() < events.get(i).getEnd()){
                comboBoxEv2.remove(events.get(i).getRoom());
            }
        }
        if (comboBoxEv2.size() == 0){
            otherRoom2.setDisable(true);
        }

        otherRoom1.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        popupForRoom(events, event1, comboBoxEv1);
                        if (!event1.getRoom().equals(event2.getRoom())) {
                            comboBoxRoom.setValue(event1.getRoom());
                            dialog.close();
                        }
                    }
                });
        otherRoom2.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        popupForRoom(events, event2, comboBoxEv2);
                        if (!event1.getRoom().equals(event2.getRoom())) {
                            comboBoxRoom.setValue(event1.getRoom());
                            dialog.close();
                        }
                    }
                });
        event1AfterEvent2.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        shift(event1,  event2, events, rooms);
                        sendMessageAboutChanges(event1);
                        semaphore.release();
                        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_EVENT);
                        dialog.close();
                    }
                });
        event2AfterEvent1.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        shift(event2,  event1, events, rooms);
                        sendMessageAboutChanges(event2);
                        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_EVENT);
                        dialog.close();
                        semaphore.release();
                    }
        });
        byHimself.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        semaphore.release();
                        dialog.close();
                    }
                });
        dialogHbox.getChildren().add(dialogVbox1);
        dialogVbox.getChildren().addAll(conflictLabel, dialogHbox);
        Scene dialogScene = new Scene(dialogVbox, 800, 350);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    public void popupWrongWorkers(ObservableList<Event> events, Event event1, Event event2, Worker retain) {
        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);

        Label conflictLabel = new Label(event1.getName() + ": " + format.format(new Date(event1.getStart()))
                + " - " + format.format(new Date(event1.getEnd())) + "\n" +
                event2.getName() + ": " + format.format(new Date(event2.getStart()))
                + " - " + format.format(new Date(event2.getEnd())) );
        conflictLabel.setFont(Font.font(null, FontWeight.BOLD, 15));

        final Stage dialog = new Stage();
        dialog.setTitle("Конфликт: сотрудник(" + retain + ") занят");
        Button removeFromEvent1 = new Button("Удалить сотдудника из списка участников события \'" + event1.getName() + "\'");
        Button removeFromEvent2 = new Button("Удалить сотдудника из списка участников события \'" + event2.getName() + "\'");
        Button event1AfterEvent2 = new Button("Начать событие \'" + event1.getName() +
                "\' после события \'" + event2.getName() + "\'");
        Button event2AfterEvent1 = new Button("Начать событие \'" + event2.getName() +
                "\' после события \'" + event1.getName() + "\'");
        Button  byHimself = new Button("Разрешить конфликт вручную");

        Label displayLabel = new Label("Как разрешить конфликт ?");
        displayLabel.setFont(Font.font(null,  14));

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner((Stage) btnOk.getScene().getWindow());

        HBox dialogHbox = new HBox(20);
        dialogHbox.setAlignment(Pos.CENTER);

        VBox dialogVbox1 = new VBox(20);
        dialogVbox1.setAlignment(Pos.CENTER);

        dialogHbox.getChildren().add(displayLabel);
        dialogVbox1.getChildren().add(removeFromEvent1);
        dialogVbox1.getChildren().add(removeFromEvent2);
        dialogVbox1.getChildren().add(event1AfterEvent2);
        dialogVbox1.getChildren().add(event2AfterEvent1);
        dialogVbox1.getChildren().add(byHimself);

        removeFromEvent1.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        event1.getWorkers().remove(retain);
                        dialog.close();
                    }
                });
        removeFromEvent2.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        event2.getWorkers().remove(retain);
                        dialog.close();
                    }
                });
        event1AfterEvent2.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        long duration = event1.getEnd() - event1.getStart();
                        event1.setStart(event2.getEnd());
                        event1.setEnd(event2.getEnd() + duration);
                        for (int i = 0; i < events.size(); i++) {
                            if (!events.get(i).equals(event2) && !events.get(i).equals(event1)){
                                if (events.get(i).getStart() >= event1.getStart() && events.get(i).getStart() < event1.getEnd() ||
                                        event1.getStart() >= events.get(i).getStart() && event1.getStart() < events.get(i).getEnd()){
                                    event1.setStart(events.get(i).getEnd());
                                    event1.setEnd(events.get(i).getEnd() + duration);
                                    sendMessageAboutChanges(event1);
                                }
                            }
                        }
                        semaphore.release();
                        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_EVENT);
                        dialog.close();
                    }
                });
        event2AfterEvent1.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        long duration = event2.getEnd() - event2.getStart();
                        event2.setStart(event1.getEnd());
                        event2.setEnd(event1.getEnd() + duration);
                        for (int i = 0; i < events.size(); i++) {
                            if (!events.get(i).equals(event2) && !events.get(i).equals(event1)){
                                if (events.get(i).getStart() >= event2.getStart() && events.get(i).getStart() < event2.getEnd() ||
                                        event2.getStart() >= events.get(i).getStart() && event2.getStart() < events.get(i).getEnd()){
                                    event2.setStart(events.get(i).getEnd());
                                    event2.setEnd(events.get(i).getEnd() + duration);
                                    sendMessageAboutChanges(event2);
                                }
                            }
                        }
                        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_EVENT);
                        dialog.close();
                    }
                });
        byHimself.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        semaphore.release();
                        dialog.close();
                    }
                });
        dialogHbox.getChildren().add(dialogVbox1);
        dialogVbox.getChildren().addAll(conflictLabel, dialogHbox);
        Scene dialogScene = new Scene(dialogVbox, 800, 350);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void popupForRoom(ObservableList<Event> events, Event event, ObservableList<Room> rooms){
        VBox dialogVbox = new VBox(20);
        dialogVbox.setAlignment(Pos.CENTER);

        final Stage dialog = new Stage();
        dialog.setTitle("Выберите комнату:");

        Button ok = new Button("Ок");
        Button cancell = new Button("Отмена");

        ComboBox<Room> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(rooms);

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner((Stage) btnOk.getScene().getWindow());

        HBox dialogHbox = new HBox(20);
        dialogHbox.setAlignment(Pos.CENTER);

        dialogHbox.getChildren().addAll(ok, cancell);
        dialogVbox.getChildren().add(comboBox);

        ok.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        event.setRoom(comboBox.getValue());
                        //comboBoxRoom.setValue(comboBox.getValue());
                        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_EVENT);
                        dialog.close();
                    }
                });
        cancell.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        semaphore.release();
                        dialog.close();
                    }
                });

        dialogVbox.getChildren().add(dialogHbox);
        Scene dialogScene = new Scene(dialogVbox, 290, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();

        dialog.close();
    }

    private void sendMessageAboutChanges(Event event){
        sendMessages(event, "Уведомление об изменениях",
                ", уведомляем о переносе события " + event.getName() +
                        ":\nвремя начала: " + new Date(event.getStart()).toString() +
                        ",\nкомната: " + event.getRoom().getName());
    }

    private void sendMessages(Event event, String subject, String message) {
        Preferences prefs = Preferences.userRoot().node("clerk");
        String gmail = prefs.get("email", "");
        String password = prefs.get("password", "");

        for (Map.Entry<String, Long> entry: sentMessagehashSet.entrySet()){
            String savedEventId = entry.getKey().substring(0, entry.getKey().indexOf('-'));
            String savedWorkerId = entry.getKey().substring(entry.getKey().indexOf('-') + 1);
            SenderMailTLS senderMailTLS = null;
            if (savedEventId.equals(event.getId()+"")){
                if (senderMailTLS == null){
                    senderMailTLS = new SenderMailTLS(gmail, password);
                }
                for (int i = 0; i < event.getWorkers().size(); i++) {
                    Worker worker = event.getWorkers().get(i);
                    int idWorker = worker.getId();
                    if (savedWorkerId.equals(idWorker + "")) {
                        senderMailTLS.sendMessage(gmail, worker.getEmail(), subject,
                                "Уважаемый " + worker.toString() + message);
                        break;
                    }
                }
            }
        }
    }

    public void deleteHandle(){
        try {
            semaphore.acquire();
            events.remove(event);
            DatabaseUtils.deleteRecordFromTable(DatabaseUtils.TABLE_EVENTS_LIST, event.getId());
            sendMessages(event, "Уведомление об отмене", ", уведомляем об отмене события " + event.getName());
            semaphore.release();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_EVENT);
        btnOk.getScene().getWindow().hide();
    }

    public void handleClickCancell(){
       btnOk.getScene().getWindow().hide();
    }

    public void onOpenDateTimeStart(){
        new DateTimePickerDialog(onDateSetListener, event.getStart(), true);
    }

    public void onOpenDateTimeEnd(){
        new DateTimePickerDialog(onDateSetListener, event.getEnd(), false);
    }

    DateTimePickerController.OnDateSetListener onDateSetListener = new DateTimePickerController.OnDateSetListener() {
        @Override
        public void onDateSet(long date, boolean isStart) {
            if (date < System.currentTimeMillis()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Неправильная дата!");
                alert.show();
            }else {
                try {
                    semaphore.acquire();
                    if (isStart) {
                        isDataChanged = true;
                        event.setStart(date);
                        btnStart.setText(format.format(new Date(date)));
                        if (event.getEnd() <= date + 10 * 60 * 1000) {
                            event.setEnd(date + 10 * 60 * 1000);
                            btnEnd.setText(format.format(date + 10 * 60 * 1000));
                        }
                    } else {
                        if (event.getStart() < date) {
                            event.setEnd(date);
                            btnEnd.setText(format.format(new Date(date)));
                        }
                    }
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
