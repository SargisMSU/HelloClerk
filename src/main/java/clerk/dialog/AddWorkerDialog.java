package clerk.dialog;

import clerk.controllers.Controller;
import clerk.controllers.dialog.AddWorkerController;
import clerk.model.Departament;
import clerk.model.Worker;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class AddWorkerDialog extends Dialog<Worker> {

    public AddWorkerDialog(Controller.OnRefreshListener onRefreshListener, ObservableList<Departament> departaments,
                           ObservableList<Worker> workers, Worker worker, Semaphore semaphore, boolean isNecessarily){
        Parent root = null;
        try {
            File file = new File("view/add_worker.fxml");
            URL url = file.toURL();
            FXMLLoader loader = new FXMLLoader(url);
            root = loader.load();
            AddWorkerController controller = loader.getController();
            Window window = getDialogPane().getScene().getWindow();
            if (!isNecessarily) {
                window.setOnCloseRequest(event -> window.hide());
            }
            controller.setModel(onRefreshListener, departaments, workers, worker, semaphore, isNecessarily);
            getDialogPane().setContent(root);
            show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
