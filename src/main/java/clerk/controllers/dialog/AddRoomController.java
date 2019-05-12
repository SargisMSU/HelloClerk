package clerk.controllers.dialog;

import clerk.controllers.Controller;
import clerk.dialog.AddEventDialog;
import clerk.dialog.AddWorkerDialog;
import clerk.model.Departament;
import clerk.model.Event;
import clerk.model.Room;
import clerk.model.Worker;
import clerk.utils.DatabaseUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class AddRoomController {

    @FXML
    TextField capacityTextField, nameTextField;

    @FXML
    Button btnOk, btnDelete;

    @FXML
    HBox hbox;

    Controller.OnRefreshListener onRefreshListener;
    ObservableList<Room> rooms;
    ObservableList<Departament> departaments;
    ObservableList<Worker> workers;
    ObservableList<Event> events;
    HashMap<String, Long> sentMessageHashMap;
    boolean isNecessary;
    Room room;
    boolean isChanged = false;
    Semaphore semaphore;

    public AddRoomController() {
    }

    @FXML
    public void initialize(){
        capacityTextField.textProperty().addListener(changeListener);
        nameTextField.textProperty().addListener(changeListener);
    }

    ChangeListener changeListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            isChanged = true;
            if (nameTextField.getText().length() == 0 || capacityTextField.getText().length() == 0){
                btnOk.setDisable(true);
            }else {
                btnOk.setDisable(false);
            }
        }
    };

    public void handleClickAdd(){
        if (isChanged) {
            String name = nameTextField.getText();
            int capacity = Integer.parseInt(capacityTextField.getText());
            if (room == null){
                room = new Room(name, capacity);
                try {
                    semaphore.acquire();
                    rooms.add(room);
                    DatabaseUtils.insertRoom(room);
                    semaphore.release();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }else {
                try {
                    semaphore.acquire();
                    room.setName(name);
                    DatabaseUtils.updateRoom(room);
                    semaphore.release();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_DEPARTAMENT);
        btnOk.getScene().getWindow().hide();
    }

    public void handleClickDelete(){
        try {
            semaphore.acquire();
            rooms.remove(room);
            DatabaseUtils.deleteRecordFromTable(DatabaseUtils.TABLE_ROOMS_LIST, room.getId());
            semaphore.release();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getRoom().equals(room)){
                events.get(i).setRoom(null);
                new AddEventDialog(onRefreshListener, departaments, workers, events, rooms,
                        events.get(i), semaphore, sentMessageHashMap, true);
            }
        }
        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_DEPARTAMENT);
        btnOk.getScene().getWindow().hide();
    }

    public void handleClickCancell(){
        btnOk.getScene().getWindow().hide();
    }

    public void setModel(Controller.OnRefreshListener onRefreshListener, ObservableList<Worker> workers,
                         ObservableList<Event> events, ObservableList<Departament> departaments,
                         ObservableList<Room> rooms, Room room, Semaphore semaphore,
                         HashMap<String, Long> sentMessagesHashMap) {
        if (room == null){
            hbox.getChildren().remove(btnDelete.getParent());
        }else {
            btnOk.setText("Ок");
        }
        this.departaments = departaments;
        this.sentMessageHashMap = sentMessagesHashMap;
        this.workers = workers;
        this.rooms = rooms;
        this.events = events;
        this.room = room;
        this.semaphore = semaphore;
        nameTextField.setText(room != null ? room.getName() : "");
        capacityTextField.setText(room != null ? room.getCapacity() + "" : "");
        this.onRefreshListener = onRefreshListener;
    }
}
