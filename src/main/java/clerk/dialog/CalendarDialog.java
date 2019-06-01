package clerk.dialog;

import clerk.controllers.Controller;
import clerk.controllers.dialog.AddWorkerController;
import clerk.controllers.dialog.CalendarController;
import clerk.model.Departament;
import clerk.model.Event;
import clerk.model.Room;
import clerk.model.Worker;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;

public class CalendarDialog extends Dialog {

    public CalendarDialog(ObservableList<Event> events, Object object){
        Parent root = null;
        try {
            File file = new File("view/calendar.fxml");
            URL url = file.toURL();
            FXMLLoader loader = new FXMLLoader(url);
            root = loader.load();
            Window window = getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(event -> window.hide());
            CalendarController controller = loader.getController();
            setResizable(true);
            controller.setModel(events, object);
            if (object instanceof Worker)
                setTitle("График занятости сотрудника " + ((Worker)object).toString());
            else
                setTitle("График занятости комнаты " + ((Room)object).getName());
            getDialogPane().setContent(root);
            show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}