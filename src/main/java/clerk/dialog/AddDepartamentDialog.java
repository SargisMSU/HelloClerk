package clerk.dialog;

import clerk.controllers.Controller;
import clerk.controllers.dialog.AddDepController;
import clerk.model.Departament;
import clerk.model.Worker;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Semaphore;

public class AddDepartamentDialog extends Dialog<Departament> {

    public AddDepartamentDialog(Controller.OnRefreshListener onRefreshListener, ObservableList<Departament> departaments,
                                ObservableList<Worker> workers, Departament departament, Semaphore semaphore){
        Parent root = null;
        try {
            File file = new File("view/add_dep.fxml");
            URL url = file.toURL();
            FXMLLoader loader = new FXMLLoader(url);
            root = loader.load();
            Window window = getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(event -> window.hide());
            AddDepController controller = loader.getController();
            controller.setModel(onRefreshListener, departaments, workers, departament, semaphore);
            getDialogPane().setContent(root);
            show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
