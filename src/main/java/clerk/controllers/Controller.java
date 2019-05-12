package clerk.controllers;

import clerk.dialog.*;
import clerk.model.Room;
import clerk.utils.DatabaseUtils;
import clerk.utils.ExcelUtils;
import clerk.model.Departament;
import clerk.model.Event;
import clerk.model.Worker;
import clerk.utils.SenderMailTLS;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.prefs.Preferences;

public class Controller {

    public interface OnRefreshListener {
        void refresh(int tableView);

        int TABLE_VIEW_EVENT = 0;
        int TABLE_VIEW_WORKER = 1;
        int TABLE_VIEW_DEPARTAMENT = 2;
        int TABLE_VIEW_ROOM = 3;
    }

    @FXML
    public TableView<Departament> tableViewDep;
    @FXML
    public TableView<Event> tableViewEvent;
    @FXML
    public TableView<Worker> tableViewWorker;
    @FXML
    public TableView<Room> tableViewRoom;
    @FXML
    public TabPane tabPane;
    @FXML
    MenuItem menuItemSendExcel;
    @FXML
    HBox hBox;

    @FXML
    TableColumn depColumnName, workerColName, workerColSurname, workerColEmail,
            workerColRoom, eventColName, eventColStart, eventColEnd, eventColDep,
            roomColumnCapacity, roomColumnName;

    ObservableList<Departament> departaments;
    ObservableList<Worker> workers;
    ObservableList<Event> events;
    ObservableList<Room> rooms;
    DateFormat format;
    Semaphore semaphore;
    SenderMailTLS senderMailTLS;
    HashMap<String, Long> sentMessagesHashMap;

    Button buttonConcat;

    public Controller() {
    }

    @FXML
    public void initialize() {
        DatabaseUtils.connToDB();
        events = FXCollections.observableArrayList();
        workers = FXCollections.observableArrayList();
        departaments = FXCollections.observableArrayList();
        rooms = FXCollections.observableArrayList();
        departaments.addAll(DatabaseUtils.readTableDepartaments());
        workers.addAll(DatabaseUtils.readTableWorkers(departaments));
        rooms.addAll(DatabaseUtils.readTableRooms());
        events.addAll(DatabaseUtils.readTableEvents(workers, departaments, rooms));

        depColumnName.setCellValueFactory(new PropertyValueFactory<Departament, String>("name"));

        eventColDep.setCellValueFactory(new PropertyValueFactory<Event, Room>("room"));
        eventColDep.setCellFactory(roomTableCellCallback);
        eventColStart.setCellValueFactory(new PropertyValueFactory<Event, Long>("start"));
        eventColStart.setCellFactory(dateTableCellCallback);
        eventColEnd.setCellValueFactory(new PropertyValueFactory<Event, Long>("end"));
        eventColEnd.setCellFactory(dateTableCellCallback);
        eventColName.setCellValueFactory(new PropertyValueFactory<Event, String>("name"));

        workerColName.setCellValueFactory(new PropertyValueFactory<Worker, String>("name"));
        workerColRoom.setCellValueFactory(new PropertyValueFactory<Worker, Departament>("departament"));
        workerColEmail.setCellValueFactory(new PropertyValueFactory<Worker, String>("email"));
        workerColSurname.setCellValueFactory(new PropertyValueFactory<Worker, String>("surname"));

        roomColumnCapacity.setCellValueFactory(new PropertyValueFactory<Room, Integer>("capacity"));
        roomColumnName.setCellValueFactory(new PropertyValueFactory<Room, String>("name"));

        tableViewDep.setItems(departaments);
        tableViewEvent.setItems(events);
        tableViewWorker.setItems(workers);
        tableViewRoom.setItems(rooms);

        format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        buttonConcat = new Button("Объединить");
        buttonConcat.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new ConcatDepsDialog(onRefreshListener, departaments, workers);
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                        if (tabPane.getSelectionModel().getSelectedIndex() == 2) {
                            hBox.getChildren().add(0, buttonConcat);
                        } else {
                            try {
                                hBox.getChildren().remove(buttonConcat);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        tableViewDep.setOnMouseClicked((MouseEvent mouseEvent) -> {
            Node node = (Node) mouseEvent.getTarget();
            if (node.getParent().getParent() instanceof TableHeaderRow) {
                return;
            }
            if (node.getTypeSelector().equals("LabeledText")) {
                node = node.getParent();
            }
            if (node.getId() != null) {
                if (node.getId().equals("depColumnName")) {
                    TableRow row = null;
                    if (node.getParent() instanceof TableRow) {
                        row = (TableRow) node.getParent();
                    }
                    if (row != null) {
                        Departament departament = (Departament) row.getItem();
                        new AddDepartamentDialog(onRefreshListener, departaments, workers, departament, semaphore);
                        System.out.println("departament = " + departament);
                    } else {
                        new AddDepartamentDialog(onRefreshListener, departaments, workers, null, semaphore);
                    }
                }
            }
        });
        tableViewEvent.setOnMouseClicked((MouseEvent mouseEvent) -> {
            Node node = (Node) mouseEvent.getTarget();
            if (node.getParent().getParent() instanceof TableHeaderRow) {
                return;
            }
            if (node.getTypeSelector().equals("LabeledText")) {
                node = node.getParent();
            }
            if (node.getId() != null) {
                TableRow row = null;
                if (node.getParent() instanceof TableRow) {
                    row = (TableRow) node.getParent();
                }
                if (row != null) {
                    Event event = (Event) row.getItem();
                    new AddEventDialog(onRefreshListener, departaments, workers, events, rooms,
                            event, semaphore, sentMessagesHashMap, false);
                    System.out.println("event = " + event);
                } else {
                    new AddEventDialog(onRefreshListener, departaments, workers, events, rooms,
                            null, semaphore, sentMessagesHashMap, false);
                }
            }
        });
        tableViewWorker.setOnMouseClicked((MouseEvent mouseEvent) -> {
            Node node = (Node) mouseEvent.getTarget();
            MouseButton button = mouseEvent.getButton();
            if (node.getParent().getParent() instanceof TableHeaderRow) {
                return;
            }
            if (node.getTypeSelector().equals("LabeledText")) {
                node = node.getParent();
            }
            if (node.getId() != null) {
                TableRow row = null;
                if (node.getParent() instanceof TableRow) {
                    row = (TableRow) node.getParent();
                }
                if(button==MouseButton.PRIMARY){
                    if (row != null) {
                        Worker worker = (Worker) row.getItem();
                        new AddWorkerDialog(onRefreshListener, departaments, workers, worker, semaphore, false);
                        System.out.println("worker = " + worker);
                    } else {
                        new AddWorkerDialog(onRefreshListener, departaments, workers, null, semaphore, false);
                    }
                }else if(button==MouseButton.SECONDARY){
                    Worker worker = (Worker) row.getItem();
                    new CalendarDialog(FXCollections.observableArrayList(events), worker);
                }
            }
        });
        tableViewRoom.setOnMouseClicked((MouseEvent mouseEvent) -> {
            Node node = (Node) mouseEvent.getTarget();
            MouseButton button = mouseEvent.getButton();
            if (node.getParent().getParent() instanceof TableHeaderRow) {
                return;
            }
            if (node.getTypeSelector().equals("LabeledText")) {
                node = node.getParent();
            }
            if (node.getId() != null) {
                TableRow row = null;
                if (node.getParent() instanceof TableRow) {
                    row = (TableRow) node.getParent();
                }
                if(button==MouseButton.PRIMARY){
                    if (row != null) {
                        Room room = (Room) row.getItem();
                        new AddRoomDialog(onRefreshListener, workers, events, departaments, rooms, room, semaphore, sentMessagesHashMap);
                        System.out.println("room = " + room);
                    } else {
                        new AddRoomDialog(onRefreshListener, workers, events, departaments, rooms, null, semaphore, sentMessagesHashMap);
                    }
                }else if(button==MouseButton.SECONDARY){
                    Room room = (Room) row.getItem();
                    new CalendarDialog(FXCollections.observableArrayList(events), room);
                }
            }
        });
        tableViewEvent.setRowFactory(new Callback<TableView<Event>, TableRow<Event>>() {
            @Override
            public TableRow<Event> call(TableView<Event> param) {
                TableRow<Event> row = new TableRow<Event>() {
                    @Override
                    protected void updateItem(Event item, boolean empty) {
                        // Сначала обязательно сбрасываем стиль.
                        setStyle("");
                        // и только после этого вызываем метод super.updateItem
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setStyle("");
                        } else {
                            Event.Priority priority = item.getPriority();
                            /*if (priority == null)
                                return;*/
                            switch (priority) {
                                case HIGH: {
                                    setStyle("-fx-background-color:#F79F81");
                                    break;
                                }
                                case NORMAL: {
                                    setStyle("-fx-background-color:#819FF7");
                                    break;
                                }
                                case LOW: {
                                    setStyle("-fx-background-color:#BEF781");
                                    break;
                                }
                            }
                        }
                    }
                };
                return row;
            }
        });

        tableViewDep.setTooltip(new Tooltip("Редактирование - левый клик,\n" +
                "добавление - левый клик на одну из пустых строк,\n"));
        tableViewEvent.setTooltip(new Tooltip("Редактирование - левый клик,\n" +
                "добавление - левый клик на одну из пустых строк,\n"));
        tableViewWorker.setTooltip(new Tooltip("Редактирование - левый клик,\n" +
                "добавление - левый клик на одну из пустых строк,\n" +
                "график занятости - правый клик."));
        tableViewRoom.setTooltip(new Tooltip("Редактирование - левый клик,\n" +
                "добавление - левый клик на одну из пустых строк,\n" +
                "график занятости - правый клик."));

        menuItemSendExcel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Platform.runLater(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Stage dialog = new Stage();
                        dialog.setTitle("Выберите сотрудника");
                        Button okBtn = new Button("Ок");
                        Button cancellBtn = new Button("Отмена");

                        dialog.initModality(Modality.WINDOW_MODAL);
                        dialog.initOwner((Stage) tableViewEvent.getScene().getWindow());

                        ComboBox<Object> comboBox = new ComboBox<>();
                        comboBox.getItems().add("Все");
                        comboBox.getItems().addAll(workers);

                        HBox dialogHbox = new HBox(20);
                        dialogHbox.setAlignment(Pos.CENTER);

                        VBox dialogVbox = new VBox(20);
                        dialogVbox.setAlignment(Pos.CENTER);

                        dialogHbox.getChildren().addAll(okBtn, cancellBtn);

                        okBtn.addEventHandler(MouseEvent.MOUSE_CLICKED,
                                new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent e) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Object comboBoxValue = comboBox.getValue();
                                                if (comboBoxValue instanceof Worker){
                                                    ExcelUtils.createFile(events, (Worker)comboBoxValue);
                                                }else {
                                                    for (int i = 0; i < workers.size(); i++) {
                                                        ExcelUtils.createFile(events, workers.get(i));
                                                    }
                                                }
                                            }
                                        }).start();
                                        dialog.close();
                                    }
                                });
                        cancellBtn.addEventHandler(MouseEvent.MOUSE_CLICKED,
                                new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent e) {
                                        dialog.close();
                                    }
                                });
                        dialogVbox.getChildren().add(comboBox);
                        dialogVbox.getChildren().add(dialogHbox);
                        Scene dialogScene = new Scene(dialogVbox, 300, 150);
                        dialog.setScene(dialogScene);
                        dialog.show();
                    }
                }));
            }
        });

        sentMessagesHashMap = new HashMap<>();
        semaphore = new Semaphore(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(15000);
                        semaphore.acquire();
                        checkEvents(events);
                        semaphore.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void checkEvents(ObservableList<Event> events) {
        Preferences prefs = Preferences.userRoot().node("clerk");
        String gmail = prefs.get("email", "");
        String password = prefs.get("password", "");
        int start = prefs.getInt("start", 10);
        int period = prefs.getInt("period", 5);

        SenderMailTLS senderMailTLS = new SenderMailTLS(gmail, password);
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getEnd() < new Date().getTime()) {
                System.out.println("event = " + event);
                events.remove(i);
                tableViewEvent.refresh();
            } else if (event.getStart() > new Date().getTime() && event.getStart() - new Date().getTime() <= start * 1000 * 60) {
                ArrayList<Worker> workers = event.getWorkers();
                for (int j = 0; j < workers.size(); j++) {
                    if (!sentMessagesHashMap.containsKey(event.getId() + "-" + workers.get(j).getId())
                            || sentMessagesHashMap.containsKey(event.getId()) &&
                            new Date().getTime() - sentMessagesHashMap.get(event.getId()) >= 60 * period * 1000) {

                        senderMailTLS.sendMessage(gmail, workers.get(j).getEmail(), "Уведомление",
                                "Уважаемый " + workers.get(j).toString() + "\n напоминаем о событии " +
                                        event.getName() + "(время начала: " + new Date(event.getStart()).toString() + ", " +
                                        "комната: " + event.getRoom().getName() + ")");
                        sentMessagesHashMap.put(event.getId() + "-" + workers.get(j).getId(), new Date().getTime());
                    }
                }
            }
        }
    }

    public void onAddDepartament() {
        new AddDepartamentDialog(onRefreshListener, departaments, workers, null, semaphore);
    }

    public void onAddWorker() {
        new AddWorkerDialog(onRefreshListener, departaments, workers, null, semaphore, false);
    }

    public void onAddEvent() {
        new AddEventDialog(onRefreshListener, departaments, workers, events, rooms,
                null, semaphore, sentMessagesHashMap, false);
    }

    public void onAddRoom() {
        new AddRoomDialog(onRefreshListener, workers, events, departaments, rooms, null, semaphore, sentMessagesHashMap);
    }

    public void onBtnAddClick() {
        switch (tabPane.getSelectionModel().getSelectedIndex()) {
            case 0: {
                onAddEvent();
                break;
            }
            case 1: {
                onAddWorker();
                break;
            }
            case 2: {
                onAddDepartament();
                break;
            }
            case 3: {
                onAddRoom();
                break;
            }
            default: {
                System.out.println("tabPane = " + tabPane.getSelectionModel().getSelectedIndex());
                System.out.println("default");
            }
        }
    }

    public void onAboutClicked() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Программа разработана ...");
        alert.show();
    }

    public void onSettingsClicked() {
        new SetSettingsDialog();
    }

    public void onExport() {
        ExcelUtils.export(events, workers, departaments, rooms);
    }

    public void onImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбрать файл:");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fileChooser.showOpenDialog(tableViewRoom.getScene().getWindow());
        if (file != null) {
            try {
                semaphore.acquire();
                ExcelUtils.importFromExcel(file.getPath(), departaments, rooms, events, workers);
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    Callback<TableColumn, TableCell> dateTableCellCallback = new Callback<TableColumn, TableCell>() {
        @Override
        public TableCell call(TableColumn param) {
            return new TextFieldTableCell(new StringConverter<Long>() {
                @Override
                public String toString(Long object) {
                    return format.format(object);
                }

                @Override
                public Long fromString(String string) {
                    try {
                        return format.parse(string).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0L;
                }
            });
        }
    };

    Callback<TableColumn, TableCell> roomTableCellCallback = new Callback<TableColumn, TableCell>() {
        @Override
        public TableCell call(TableColumn param) {
            return new TextFieldTableCell(new StringConverter<Room>() {
                @Override
                public String toString(Room room) {
                    return room.getName();
                }
                @Override
                public Room fromString(String string) {
                    return null;
                }
            });
        }
    };

    OnRefreshListener onRefreshListener = new OnRefreshListener() {
        @Override
        public void refresh(int tableViewNum) {
            switch (tableViewNum) {
                case TABLE_VIEW_DEPARTAMENT: {
                    tableViewDep.refresh();
                    tableViewWorker.refresh();
                    break;
                }
                case TABLE_VIEW_EVENT: {
                    tableViewEvent.refresh();
                    break;
                }
                case TABLE_VIEW_WORKER: {
                    tableViewWorker.refresh();
                    break;
                }
                case TABLE_VIEW_ROOM: {
                    tableViewRoom.refresh();
                    tableViewEvent.refresh();
                    break;
                }
            }
        }
    };
}
