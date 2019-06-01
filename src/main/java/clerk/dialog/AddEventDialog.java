package clerk.dialog;

import clerk.controllers.Controller;
import clerk.controllers.dialog.AddEventController;
import clerk.model.Departament;
import clerk.model.Event;
import clerk.model.Room;
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

public class AddEventDialog extends Dialog<Worker> {

    public AddEventDialog(Controller.OnRefreshListener onRefreshListener, ObservableList<Departament> departaments,
                          ObservableList<Worker>workers, ObservableList<Event> events, ObservableList<Room> rooms,
                          Event event, HashMap<String, Long> sentMessageHashSet, boolean isNecessary){
        Parent root = null;
        try {
            File file = new File("view/add_event.fxml");
            URL url = file.toURL();
            FXMLLoader loader = new FXMLLoader(url);
            root = loader.load();
            AddEventController controller = loader.getController();
            Window window = getDialogPane().getScene().getWindow();
            if (!isNecessary){
                window.setOnCloseRequest(ev -> window.hide());
            }
            controller.setModel(onRefreshListener, departaments, workers, events, rooms,
                    event, sentMessageHashSet, isNecessary);
            getDialogPane().setContent(root);
            show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
