package clerk.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class Event {

    private Room room;
    private String name;
    private long start, end;
    private ArrayList<Worker> workers;
    private Priority priority;
    private int id;
    private String note;

    public void changeStart(long newStart) {
        long duration = end - start;
        setStart(newStart);
        setEnd(newStart + duration);
    }

    public boolean isInvolvedDirector() {
        for (int i = 0; i < workers.size(); i++) {
            Worker worker = workers.get(i);
            if (worker.getPosition() == Worker.Position.DIRECTOR) {
                return true;
            }
        }
        return false;
    }

    public int getWeight() {
        return workers.stream().mapToInt(worker -> worker.getPosition().getCode()).sum();
    }

    public enum Priority{
        HIGH(15), NORMAL(10), LOW(5);

        int code;

        Priority(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Priority getPriorityByCode(int code){
            switch (code){
                case 15:{
                    return Priority.HIGH;
                }
                case 10:{
                    return Priority.NORMAL;
                }
                case 5:{
                    return Priority.LOW;
                }
                default:
                    return null;
            }
        }

        @Override
        public String toString() {
            switch (code){
                case 15:{
                    return "Очень важное событие";
                }
                case 10:{
                    return "Важное событие";
                }
                case 5:{
                    return "Не очень важное событие";
                }
                default:
                    return null;
            }
        }
    }

    public Event(Room room, String name, long start, long end, ArrayList<Worker> workers,
                 Priority priority, String note) {
        this.room = room;
        this.name = name;
        this.start = start;
        this.end = end;
        this.workers = workers;
        this.priority = priority;
        this.note = note;
    }

    public Event(int id, Room room, String name, long start, long end, ArrayList<Worker> workers,
                 Priority priority, String note) {
        this.room = room;
        this.name = name;
        this.start = start;
        this.end = end;
        this.workers = workers;
        this.priority = priority;
        this.id = id;
        this.note = note;
    }

    public Event() {
        this.start = Calendar.getInstance().getTimeInMillis();
        this.end = Calendar.getInstance().getTimeInMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public ArrayList<Worker> getWorkers() {
        return workers;
    }

    public void setWorkers(ArrayList<Worker> workers) {
        this.workers = workers;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return id == event.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Event findEvent(ArrayList<Event> events, int uuid){
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId() == uuid){
                return events.get(i);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Event{" +
                "room=" + room +
                ", name='" + name + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", workers=" + workers +
                ", priority=" + priority +
                ", id=" + id +
                ", note='" + note + '\'' +
                '}';
    }
}
