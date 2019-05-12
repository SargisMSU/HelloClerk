package clerk.controllers.dialog;

import clerk.controllers.Controller;
import clerk.model.Departament;
import clerk.model.Worker;
import clerk.utils.DatabaseUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ConcatDepsController {
    @FXML
    TextField nameTextField;
    @FXML
    ComboBox<Departament> comboBoxDep0, comboBoxDep1;
    @FXML
    Button btnOk;
    @FXML
    HBox hBox;

    private ObservableList<Departament> departaments;
    private ObservableList<Worker> workers;

    private Controller.OnRefreshListener onRefreshListener;

    public ConcatDepsController() {
    }

    @FXML
    public void initialize(){
        nameTextField.textProperty().addListener(changeListener);
        comboBoxDep0.valueProperty().addListener(changeListener);
        comboBoxDep1.valueProperty().addListener(changeListener);
    }

    private ChangeListener changeListener = new ChangeListener() {
        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            if (nameTextField.getText().length() == 0 || comboBoxDep0.getValue() == null ||
                comboBoxDep1.getValue() == null || comboBoxDep0.getValue() == comboBoxDep1.getValue()){
                btnOk.setDisable(true);
            }else {
                btnOk.setDisable(false);
            }
        }
    };

    public void handleClickOk() {
        String name = nameTextField.getText();
        Departament dep0 = comboBoxDep0.getValue();
        Departament dep1 = comboBoxDep1.getValue();

        Departament departament = new Departament(name);
        DatabaseUtils.insertDepartament(departament);

        for (int i = 0; i < workers.size(); i++) {
            if (workers.get(i).getDepartament().equals(dep0) ||
                    workers.get(i).getDepartament().equals(dep1)){
                workers.get(i).setDepartament(departament);
                DatabaseUtils.updateWorker(workers.get(i));
            }
        }
        DatabaseUtils.deleteRecordFromTable(DatabaseUtils.TABLE_DEPART_LIST, dep0.getId());
        DatabaseUtils.deleteRecordFromTable(DatabaseUtils.TABLE_DEPART_LIST, dep1.getId());
        departaments.remove(dep0);
        departaments.remove(dep1);
        departaments.add(departament);

        onRefreshListener.refresh(Controller.OnRefreshListener.TABLE_VIEW_WORKER);
        btnOk.getScene().getWindow().hide();
    }

    public void handleClickCancell(){
        btnOk.getScene().getWindow().hide();
    }

    public void setModel(Controller.OnRefreshListener onRefreshListener,
                         ObservableList<Departament> departaments, ObservableList<Worker> workers) {
        this.workers = workers;
        this.departaments = departaments;
        this.onRefreshListener = onRefreshListener;
        comboBoxDep0.setItems(departaments);
        comboBoxDep1.setItems(departaments);
    }
}
