package clerk.controllers.dialog;

import clerk.controllers.Controller;
import clerk.model.Event;
import clerk.utils.DatabaseUtils;
import clerk.model.Departament;
import clerk.model.Worker;
import clerk.utils.SenderMailTLS;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.prefs.Preferences;

public class AddWorkerController {

    @FXML
    TextField nameTextField, surnameTextField, emailTextField;
    @FXML
    ComboBox<Departament> comboBoxDep;
    @FXML
    ComboBox<Worker.Position> comboBoxPosition;
    @FXML
    Button btnOk, btnDelete, btnCancell;
    @FXML
    HBox hBox;

    private ObservableList<Worker> workers;
    private Worker worker;
    private boolean isChanged = false;
    private Controller.OnRefreshListener onRefreshListener;
    private Semaphore semaphore;

    public AddWorkerController() {
    }

    @FXML
    public void initialize(){
        nameTextField.textProperty().addListener(changeListener);
        surnameTextField.textProperty().addListener(changeListener);
        emailTextField.textProperty().addListener(changeListener);
        comboBoxDep.valueProperty().addListener(changeListener);
        comboBoxPosition.valueProperty().addListener(changeListener);
        comboBoxPosition.getItems().addAll(Worker.Position.DIRECTOR, Worker.Position.HEAD_OF_DEP,
                        Worker.Position.SPECIALIST, Worker.Position.WORKER);
    }

    ChangeListener changeListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            isChanged = true;
            if (nameTextField.getText().length() == 0 || surnameTextField.getText().length() == 0 ||
                    emailTextField.getText().length() == 0 || comboBoxDep.getValue() == null ||
                comboBoxPosition.getValue() == null){
                btnOk.setDisable(true);
            }else {
                btnOk.setDisable(false);
            }
        }
    };

    public void handleClickAdd(){
        if (isChanged) {
            String name = nameTextField.getText();
            String surname = surnameTextField.getText();
            String email = emailTextField.getText();
            Departament dep = comboBoxDep.getValue();
            Worker.Position position = comboBoxPosition.getValue();
            if (worker == null){
                worker = new Worker(name, surname, email, dep, position);
                try {
                    semaphore.acquire();
                    workers.add(worker);
                    DatabaseUtils.insertWorker(worker);
                    semaphore.release();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }else {
                worker.setName(name);
                worker.setSurname(surname);
                worker.setEmail(email);
                worker.setDepartament(dep);
                worker.setPosition(position);
                DatabaseUtils.updateWorker(worker);
            }

            onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_WORKER);
        }
        btnOk.getScene().getWindow().hide();
    }

    public void onDelete(){
        try {
            semaphore.acquire();
            workers.remove(worker);
            DatabaseUtils.deleteRecordFromTable(DatabaseUtils.TABLE_WORKERS_LIST, worker.getId());
            semaphore.release();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_WORKER);
        btnOk.getScene().getWindow().hide();
    }

    public void handleClickCancell(){
        btnOk.getScene().getWindow().hide();
    }

    public void setModel(Controller.OnRefreshListener onRefreshListener, ObservableList<Departament> departaments,
                         ObservableList<Worker> workers, Worker worker, Semaphore semaphore, boolean isNecessarily) {
        this.workers = workers;
        this.worker = worker;
        this.onRefreshListener = onRefreshListener;
        this.semaphore = semaphore;
        comboBoxDep.setItems(departaments);
        if (worker != null){
            nameTextField.setText(worker.getName());
            surnameTextField.setText(worker.getSurname());
            emailTextField.setText(worker.getEmail());
            comboBoxDep.setValue(worker.getDepartament());
            comboBoxPosition.setValue(worker.getPosition());
            btnOk.setText("Ок");
        }else {
            hBox.getChildren().remove(btnDelete.getParent());
        }
        if (isNecessarily){
            btnCancell.setDisable(true);
        }
    }
}
