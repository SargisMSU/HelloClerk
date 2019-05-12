package clerk.utils;

import clerk.model.Departament;
import clerk.model.Event;
import clerk.model.Room;
import clerk.model.Worker;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseUtils {

    private static Connection conn;
    private static Statement statmt;
    private static ResultSet resSet;

    public static final String TABLE_WORKERS_LIST = "WorkersList";
    public static final String TABLE_DEPART_LIST = "DepartList";
    public static final String TABLE_EVENT_WORKER = "Event_Worker";
    public static final String TABLE_EVENTS_LIST = "EventsList";
    public static final String TABLE_ROOMS_LIST = "RoomsList";

    // --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
    public static void connToDB() {
        try {
            conn = null;
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:db/local_db.db");
            statmt = conn.createStatement();
        }catch (ClassNotFoundException|SQLException e){
            e.printStackTrace();
        }
    }

    // -------- Чтение таблицы сотрудников--------
    public static ArrayList<Worker> readTableWorkers(ObservableList<Departament> departaments){
        ArrayList<Worker> workers = new ArrayList<>();
        try {
            resSet = statmt.executeQuery("SELECT * FROM WorkersList");

            while (resSet.next()) {
                int id = resSet.getInt("id");
                String name = resSet.getString("name");
                String surname = resSet.getString("surname");
                String email = resSet.getString("email");
                int depart = resSet.getInt("depart");
                int pos = resSet.getInt("position");
                Worker.Position position = Worker.Position.getPositionByCode(pos);
                workers.add(new Worker(id, name, surname, email, Departament.findDepartament(departaments, depart), position));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return workers;
    }

    // -------- Чтение таблицы отделов--------
    public static ArrayList<Departament> readTableDepartaments() {
        ArrayList<Departament> departaments = new ArrayList<>();
        try {
            resSet = statmt.executeQuery("SELECT * FROM DepartList");

            while (resSet.next()) {
                int id = resSet.getInt("id");
                String name = resSet.getString("name");
                departaments.add(new Departament(id, name));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return departaments;
    }

    // --------Чтение таблицы отделов--------
    public static ArrayList<Event> readTableEvents(ObservableList<Worker> workers,
                                                   ObservableList<Departament> departaments,
                                                   ObservableList<Room> rooms) {
        ArrayList<Event> events = new ArrayList<>();
        try {
            resSet = statmt.executeQuery("SELECT * FROM Event_Worker");
            HashMap<Integer, ArrayList<Worker>> eventWorker = new HashMap<>();
            while (resSet.next()) {
                int event = resSet.getInt("event");
                int worker = resSet.getInt("worker");
                ArrayList<Worker> hashSet;
                if (!eventWorker.containsKey(event)){
                    hashSet = new ArrayList<>();
                    eventWorker.put(event, hashSet);
                }else {
                    hashSet = eventWorker.get(event);
                }
                hashSet.add(Worker.findWorker(workers, worker));
            }

            resSet = statmt.executeQuery("SELECT * FROM EventsList");

            while (resSet.next()) {
                int id = resSet.getInt("id");
                String name = resSet.getString("name");
                int roomId = resSet.getInt("room");
                long start = resSet.getLong("start");
                long end = resSet.getLong("end");
                int priority = resSet.getInt("priority");
                String note = resSet.getString("note");
                Event.Priority priorityObject = Event.Priority.getPriorityByCode(priority);
                ArrayList<Worker> tempWorkers = eventWorker.containsKey(id) ? eventWorker.get(id) : new ArrayList<>();
                events.add(new Event(id, Room.findRoom(rooms, roomId), name,
                        start, end, tempWorkers, priorityObject, note));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return events;
    }

    // Добавление отдела в БД
    public static void insertDepartament(Departament departament) {
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO DepartList(name) " + "VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, departament.getName());
            statement.execute();
            ResultSet gk = statement.getGeneratedKeys();
            if(gk.next()) {
                int anInt = gk.getInt(1);
                departament.setId(anInt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertRoom(Room room) {
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO RoomsList(name, capacity) " + "VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, room.getName());
            statement.setObject(2, room.getCapacity());
            statement.execute();
            ResultSet gk = statement.getGeneratedKeys();
            if(gk.next()) {
                int anInt = gk.getInt(1);
                room.setId(anInt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateDepartament(int id, String newName) {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE DepartList SET name=? WHERE id=?")) {
            statement.setObject(1, newName);
            statement.setObject(2, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateRoom(Room room) {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE DepartList SET name=?, capacity=? WHERE id=?")) {
            statement.setObject(1, room.getName());
            statement.setObject(2, room.getCapacity());
            statement.setObject(3, room.getId());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Добавление работника в БД
    public static void insertWorker(Worker worker) {
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO WorkersList(name, surname, email, depart, position) " + "VALUES(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, worker.getName());
            statement.setObject(2, worker.getSurname());
            statement.setObject(3, worker.getEmail());
            statement.setObject(4, worker.getDepartament().getId());
            statement.setObject(5, worker.getPosition().getCode());
            statement.execute();
            ResultSet gk = statement.getGeneratedKeys();
            if(gk.next()) {
                int id = gk.getInt(1);
                worker.setId(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateWorker(Worker worker) {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE WorkersList SET name=?, surname=?, email=?, depart=?, position=? WHERE id=?")) {
            statement.setObject(1, worker.getName());
            statement.setObject(2, worker.getSurname());
            statement.setObject(3, worker.getEmail());
            statement.setObject(4, worker.getDepartament().getId());
            statement.setObject(5, worker.getPosition().getCode());
            statement.setObject(6, worker.getId());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Добавление события в БД
    public static void insertEvent(Event event) {
        try (PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO EventsList(name, room, start, end, priority, note) VALUES(?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, event.getName());
            statement.setObject(2, event.getRoom().getId());
            statement.setObject(3, event.getStart());
            statement.setObject(4, event.getEnd());
            statement.setObject(5, event.getPriority().getCode());
            statement.setObject(6, event.getNote());
            statement.execute();
            ResultSet gk = statement.getGeneratedKeys();
            if(gk.next()) {
                int id = gk.getInt(1);
                event.setId(id);
            }
            updateEventWorker(event.getId(), event.getWorkers());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateEvent(Event event) {
        try (PreparedStatement statement = conn.prepareStatement(
                "UPDATE EventsList SET name=?, room=?, start=?, end=?, priority=?, note=? WHERE id=?")) {
            statement.setObject(1, event.getName());
            statement.setObject(2, event.getRoom().getId());
            statement.setObject(3, event.getStart());
            statement.setObject(4, event.getEnd());
            statement.setObject(5, event.getPriority().getCode());
            statement.setObject(6, event.getNote());
            statement.setObject(7, event.getId());
            statement.execute();
            updateEventWorker(event.getId(), event.getWorkers());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateEventWorker(int eventId, ArrayList<Worker> workers){
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM Event_Worker WHERE event=?")) {
            statement.setObject(1, eventId);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < workers.size(); i++) {
            Worker worker = workers.get(i);
            try (PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO Event_Worker(event, worker) VALUES(?, ?)")) {
                statement.setObject(1, eventId);
                statement.setObject(2, worker.getId());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteRecordFromTable(String tablename, int recordId){
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM " + tablename + " WHERE id=?")) {
            statement.setObject(1, recordId);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Room> readTableRooms() {
        ArrayList<Room> rooms = new ArrayList<>();
        try {
            resSet = statmt.executeQuery("SELECT * FROM RoomsList");

            while (resSet.next()) {
                int id = resSet.getInt("id");
                String name = resSet.getString("name");
                int capacity = resSet.getInt("capacity");
                rooms.add(new Room(id, name, capacity));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return rooms;
    }

    public static void deleteAllFromTable(String tablename){
        try {
            PreparedStatement statement = conn.prepareStatement("DELETE FROM " + tablename);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllRecords(){
        deleteAllFromTable(TABLE_EVENTS_LIST);
        deleteAllFromTable(TABLE_WORKERS_LIST);
        deleteAllFromTable(TABLE_EVENT_WORKER);
        deleteAllFromTable(TABLE_DEPART_LIST);
        deleteAllFromTable(TABLE_ROOMS_LIST);
    }

    // --------Закрытие--------
    public static void closeDB() throws ClassNotFoundException, SQLException {
        conn.close();
        statmt.close();
        resSet.close();

        System.out.println("Соединения закрыты");
    }
}
