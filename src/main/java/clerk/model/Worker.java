package clerk.model;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Worker {

    public enum Position{
        DIRECTOR(15), HEAD_OF_DEP(10), SPECIALIST(5), WORKER(3);

        int code;

        Position(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Position getPositionByCode(int code){
            switch (code){
                case 15:{
                    return DIRECTOR;
                }
                case 10:{
                    return HEAD_OF_DEP;
                }
                case 5:{
                    return SPECIALIST;
                }
                case 3: {
                    return WORKER;
                }
                default:
                    return null;
            }
        }

        @Override
        public String toString() {
            switch (code){
                case 15:{
                    return "Директор";
                }
                case 10:{
                    return "Начальник отдела";
                }
                case 5:{
                    return "Специалист";
                }
                case 3: {
                    return "Обслуживающий персонал";
                }
                default:
                    return null;
            }
        }
    }

    private int id;
    private String name;
    private String surname;
    private String email;
    private Departament departament;
    private Position position;

    public Worker(int id, String name, String surname, String email, Departament departament, Position position) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.departament = departament;
        this.position = position;
    }

    public Worker(String name, String surname, String email, Departament departament, Position position) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.departament = departament;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Departament getDepartament() {
        return departament;
    }

    public void setDepartament(Departament departament) {
        this.departament = departament;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return name + " " + surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Worker worker = (Worker) o;
        return id == worker.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Worker findWorker(ObservableList<Worker> workers, int uuid){
        for (int i = 0; i < workers.size(); i++) {
            if (workers.get(i).getId() == uuid){
                return workers.get(i);
            }
        }
        return null;
    }

    public static HashMap<String, HashSet<Worker>> getGroupsOfWorkers(ObservableList<Worker> workers){
        HashMap<String, HashSet<Worker>> hashMap = new HashMap<>();
        for (int i = 0; i < workers.size(); i++) {
            if (hashMap.containsKey(workers.get(i).getDepartament().getName())){
                hashMap.get(workers.get(i).getDepartament().getName()).add(workers.get(i));
            }else {
                HashSet<Worker> hashSet = new HashSet<>();
                hashSet.add(workers.get(i));
                hashMap.put(workers.get(i).getDepartament().getName(), hashSet);
            }
        }
        return hashMap;
    }

}
