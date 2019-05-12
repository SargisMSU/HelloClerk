package clerk.controllers.dialog;

import clerk.controllers.Controller;
import clerk.dialog.AddWorkerDialog;
import clerk.model.Worker;
import clerk.utils.DatabaseUtils;
import clerk.model.Departament;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import javax.xml.crypto.Data;
import java.util.concurrent.Semaphore;

public class AddDepController {

    @FXML
    TextField textFieldDep;

    @FXML
    Button btnAddDep, btnDelete;

    @FXML
    HBox hbox;

    Controller.OnRefreshListener onRefreshListener;
    ObservableList<Departament> departaments;
    ObservableList<Worker> workers;
    Departament departament;
    boolean isChanged = false;
    Semaphore semaphore;

    public AddDepController() {
    }

    @FXML
    public void initialize(){
        textFieldDep.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                isChanged = true;
                if (newValue.length() == 0){
                    btnAddDep.setDisable(true);
                }else {
                    btnAddDep.setDisable(false);
                }
            }
        });
    }

    public void handleClickAdd(){
        if (isChanged) {
            String text = textFieldDep.getText();
            if (departament == null){
                departament = new Departament(text);
                try {
                    semaphore.acquire();
                    departaments.add(departament);
                    DatabaseUtils.insertDepartament(departament);
                    semaphore.release();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }else {
                try {
                    semaphore.acquire();
                    departament.setName(text);
                    DatabaseUtils.updateDepartament(departament.getId(), text);
                    semaphore.release();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_DEPARTAMENT);
        btnAddDep.getScene().getWindow().hide();
    }

    public void handleClickCancell(){
        btnAddDep.getScene().getWindow().hide();
    }

    public void onDelete(){
        try {
            semaphore.acquire();
            departaments.remove(departament);
            DatabaseUtils.deleteRecordFromTable(DatabaseUtils.TABLE_DEPART_LIST, departament.getId());
            semaphore.release();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        for (int i = 0; i < workers.size(); i++) {
            if (workers.get(i).getDepartament().equals(departament)){
                workers.get(i).setDepartament(null);
                new AddWorkerDialog(onRefreshListener, departaments, workers, workers.get(i), semaphore, true);
            }
        }
        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_DEPARTAMENT);
        btnAddDep.getScene().getWindow().hide();
    }

    public void setModel(Controller.OnRefreshListener onRefreshListener, ObservableList<Departament> departaments,
                         ObservableList<Worker> workers, Departament departament, Semaphore semaphore) {
        if (departament == null){
            hbox.getChildren().remove(btnDelete);
        }else {
            btnAddDep.setText("Ok");
        }
        this.departaments = departaments;
        this.workers = workers;
        this.departament = departament;
        this.semaphore = semaphore;
        textFieldDep.setText(departament != null ? departament.getName() : "");
        this.onRefreshListener = onRefreshListener;
    }
}
