package clerk.dialog;

import clerk.controllers.Controller;
import clerk.controllers.dialog.AddRoomController;
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
import java.util.concurrent.Semaphore;

public class AddRoomDialog extends Dialog<Room> {

    public AddRoomDialog(Controller.OnRefreshListener onRefreshListener, ObservableList<Worker> workers,
                         ObservableList<Event> events, ObservableList<Departament> departaments,
                         ObservableList<Room> rooms, Room room, Semaphore semaphore,
                         HashMap<String, Long> hashMap) {
        Parent root = null;
        try {
            File file = new File("view/add_room.fxml");
            URL url = file.toURL();
            FXMLLoader loader = new FXMLLoader(url);
            root = loader.load();
            Window window = getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(event -> window.hide());
            AddRoomController controller = loader.getController();
            controller.setModel(onRefreshListener, workers, events, departaments, rooms, room, semaphore, hashMap);
            getDialogPane().setContent(root);
            show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
