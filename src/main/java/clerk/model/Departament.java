package clerk.model;

import javafx.collections.ObservableList;
import java.util.Objects;

public class Departament {

    private String name;
    private int id;

    public Departament() {
    }

    public Departament(String name) {
        this.name = name;
    }

    public Departament(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Departament that = (Departament) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }

    public static Departament findDepartament(ObservableList<Departament> departaments, int id){
        for (int i = 0; i < departaments.size(); i++) {
            if (departaments.get(i).getId() == id){
                return departaments.get(i);
            }
        }
        return null;
    }

    public static int findIdByDepartamentName(ObservableList<Departament> departaments, String name){
        for (int i = 0; i < departaments.size(); i++) {
            if (departaments.get(i).getName().equals(name)){
                return departaments.get(i).getId();
            }
        }
        return -1;
    }
}
