package clerk.model;

import javafx.collections.ObservableList;

public class Room {

    private int id;
    private String name;
    private int capacity;

    public Room(int id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public static Room findRoom(ObservableList<Room> rooms, int roomId) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getId() == roomId){
                return rooms.get(i);
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    //stugi!!!!!!!!!!!!!!!
    public Event busyWith(ObservableList<Event> events, long startCompare, long endCompare){
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event.getStart() >= startCompare && event.getStart() <= endCompare ||
                startCompare >= event.getStart() && startCompare <= event.getEnd()){
                return event;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name + " (" + capacity + ")";
    }
}
