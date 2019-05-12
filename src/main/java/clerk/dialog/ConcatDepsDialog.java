package clerk.dialog;

import clerk.controllers.Controller;
import clerk.controllers.dialog.AddWorkerController;
import clerk.controllers.dialog.ConcatDepsController;
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

public class ConcatDepsDialog  extends Dialog<Worker> {

    public ConcatDepsDialog(Controller.OnRefreshListener onRefreshListener,
                            ObservableList<Departament> departaments, ObservableList<Worker>workers){
        Parent root = null;
        try {
            File file = new File("view/concat.fxml");
            URL url = file.toURL();
            FXMLLoader loader = new FXMLLoader(url);
            root = loader.load();
            ConcatDepsController controller = loader.getController();
            Window window = getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(event -> window.hide());
            controller.setModel(onRefreshListener, departaments, workers);
            getDialogPane().setContent(root);
            show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}