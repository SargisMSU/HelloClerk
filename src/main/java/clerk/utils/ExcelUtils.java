package clerk.utils;

import clerk.model.Departament;
import clerk.model.Event;
import clerk.model.Room;
import clerk.model.Worker;
import javafx.collections.ObservableList;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.prefs.Preferences;

public class ExcelUtils {

    static DateFormat format;

    public static void createFile(ObservableList<Event> events, String fileName){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("События");

        format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Название");
        header.createCell(1).setCellValue("Место");
        header.createCell(2).setCellValue("Начало");
        header.createCell(3).setCellValue("Конец");
        header.createCell(4).setCellValue("Участники");

        int rowCount = 1;

        for (Event event: events) {
            Row row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(event.getName());
            row.createCell(1).setCellValue(event.getRoom().getName());
            row.createCell(2).setCellValue(format.format(event.getStart()));
            row.createCell(3).setCellValue(format.format(event.getEnd()));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < event.getWorkers().size(); i++) {
                builder.append(event.getWorkers().get(i).getName()).append(" ")
                        .append(event.getWorkers().get(i).getSurname()).append(", ");
            }
            if (event.getWorkers().size() > 0){
                builder.delete(builder.length() - 2, builder.length());
            }
            row.createCell(4).setCellValue(builder.toString());
        }
        try {
            FileOutputStream outputStream;
            if (!Files.exists(Paths.get(fileName + ".xls"))) {
                outputStream = new FileOutputStream(fileName + ".xls");
            } else {
                int k = 0;
                while (Files.exists(Paths.get(fileName + "(" + k + ")" + ".xls"))) {
                    k++;
                }
                outputStream = new FileOutputStream(fileName + "(" + k + ")" + ".xls");
            }

            workbook.write(outputStream);
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void createFile(ObservableList<Event> events, Worker worker){
        String fileName = worker.toString().replace(" ", "") + "Отчет.xls";
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("События");

        format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Название");
        header.createCell(1).setCellValue("Комната");
        header.createCell(2).setCellValue("Начало");
        header.createCell(3).setCellValue("Конец");
        header.createCell(4).setCellValue("Участники");
        header.createCell(5).setCellValue("Приоритет");
        header.createCell(6).setCellValue("Примечание");

        int rowCount = 1;

        for (Event event: events) {
            if (event.getWorkers().contains(worker)) {
                Row row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(event.getName());
                row.createCell(1).setCellValue(event.getRoom().getName());
                row.createCell(2).setCellValue(format.format(event.getStart()));
                row.createCell(3).setCellValue(format.format(event.getEnd()));
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < event.getWorkers().size(); i++) {
                    builder.append(event.getWorkers().get(i).getName()).append(" ")
                            .append(event.getWorkers().get(i).getSurname()).append(", ");
                }
                if (event.getWorkers().size() > 0) {
                    builder.delete(builder.length() - 2, builder.length());
                }
                row.createCell(4).setCellValue(builder.toString());
                row.createCell(5).setCellValue(event.getPriority().toString());
                row.createCell(6).setCellValue(event.getNote());
            }
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(fileName, false);
            workbook.write(outputStream);
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        Preferences prefs = Preferences.userRoot().node("clerk");
        String gmail = prefs.get("email", "");
        String password = prefs.get("password", "");
        SenderMailTLS senderMailTLS = new SenderMailTLS(gmail, password);
        senderMailTLS.sendFile(gmail, worker.getEmail(), "Расписание", fileName);
    }

    public static void export(ObservableList<Event> events, ObservableList<Worker> workers,
                              ObservableList<Departament> departaments,
                              ObservableList<Room> rooms){

        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet sheetEvent = workbook.createSheet("События");
        HSSFSheet sheetWorker = workbook.createSheet("Сотрудники");
        HSSFSheet sheetEventWorker = workbook.createSheet("Event_Worker");
        HSSFSheet sheetDepartament = workbook.createSheet("Отделы");
        HSSFSheet sheetRoom = workbook.createSheet("Комнаты");


        Row headerEvent = sheetEvent.createRow(0);
        headerEvent.createCell(0).setCellValue("id");
        headerEvent.createCell(1).setCellValue("Название");
        headerEvent.createCell(2).setCellValue("Место");
        headerEvent.createCell(3).setCellValue("Начало");
        headerEvent.createCell(4).setCellValue("Конец");
        headerEvent.createCell(5).setCellValue("Приоритет");
        headerEvent.createCell(6).setCellValue("Примечание");

        Row headerEventWorker = sheetEventWorker.createRow(0);
        headerEventWorker.createCell(0).setCellValue("id события");
        headerEventWorker.createCell(1).setCellValue("id сотрудника");

        Row headerDepartament = sheetDepartament.createRow(0);
        headerDepartament.createCell(0).setCellValue("id");
        headerDepartament.createCell(1).setCellValue("Название");

        Row headerWorker = sheetWorker.createRow(0);
        headerWorker.createCell(0).setCellValue("id");
        headerWorker.createCell(1).setCellValue("Имя");
        headerWorker.createCell(2).setCellValue("Фамилия");
        headerWorker.createCell(3).setCellValue("email");
        headerWorker.createCell(4).setCellValue("Отдел");

        Row headerRoom = sheetRoom.createRow(0);
        headerRoom.createCell(0).setCellValue("id");
        headerRoom.createCell(1).setCellValue("Название");
        headerRoom.createCell(2).setCellValue("Вместимость");

        int rowCountEvent = 1;
        int rowCountEventWorker = 1;
        int rowCountWorker = 1;
        int rowCountRoom = 1;
        int rowCountDepartament = 1;

        for (Event event: events) {
            Row row = sheetEvent.createRow(rowCountEvent++);
            row.createCell(0).setCellValue(event.getId());
            row.createCell(1).setCellValue(event.getName());
            row.createCell(2).setCellValue(event.getRoom().getId());
            row.createCell(3).setCellValue(event.getStart() + "");
            row.createCell(4).setCellValue(event.getEnd() + "");
            row.createCell(5).setCellValue(event.getPriority().getCode());
            row.createCell(6).setCellValue(event.getNote());
            for (int i = 0; i < event.getWorkers().size(); i++) {
                Row row1 = sheetEventWorker.createRow(rowCountEventWorker++);
                row1.createCell(0).setCellValue(event.getId());
                row1.createCell(1).setCellValue(event.getWorkers().get(i).getId());
            }
        }

        for (Departament departament :departaments) {
            Row row = sheetDepartament.createRow(rowCountDepartament++);
            row.createCell(0).setCellValue(departament.getId());
            row.createCell(1).setCellValue(departament.getName());
        }

        for (Worker worker :workers) {
            Row row = sheetWorker.createRow(rowCountWorker++);
            row.createCell(0).setCellValue(worker.getId());
            row.createCell(1).setCellValue(worker.getName());
            row.createCell(2).setCellValue(worker.getSurname());
            row.createCell(3).setCellValue(worker.getEmail());
            row.createCell(4).setCellValue(worker.getDepartament().getId());
            row.createCell(5).setCellValue(worker.getPosition().getCode());
        }

        for (Room room :rooms) {
            Row row = sheetRoom.createRow(rowCountRoom++);
            row.createCell(0).setCellValue(room.getId());
            row.createCell(1).setCellValue(room.getName());
            row.createCell(2).setCellValue(room.getCapacity());
        }

        for (int i = 0; i < 7; i++) {
            sheetEvent.autoSizeColumn(i);
            sheetWorker.autoSizeColumn(i);
            sheetEventWorker.autoSizeColumn(i);
            sheetDepartament.autoSizeColumn(i);
            sheetRoom.autoSizeColumn(i);
        }

        try {
            FileOutputStream outputStream = new FileOutputStream("export.xls", false);
            workbook.write(outputStream);
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void importFromExcel(String url, ObservableList<Departament> departaments,
                                       ObservableList<Room> rooms, ObservableList<Event> events,
                                       ObservableList<Worker> workers){
        HashMap<Integer, ArrayList<Worker>> eventWorker = new HashMap<>();
        events.clear();
        workers.clear();
        departaments.clear();
        rooms.clear();
        try {
            HSSFWorkbook myExcelBook = new HSSFWorkbook(new FileInputStream(url));

            HSSFSheet sheetEvent = myExcelBook.getSheet("События");
            HSSFSheet sheetEventWorker = myExcelBook.getSheet("Event_Worker");
            HSSFSheet sheetWorker = myExcelBook.getSheet("Сотрудники");
            HSSFSheet sheetDepartament = myExcelBook.getSheet("Отделы");
            HSSFSheet sheetRoom = myExcelBook.getSheet("Комнаты");

            int lastRowNum = sheetDepartament.getLastRowNum();
            int firstRowNum = sheetDepartament.getFirstRowNum() + 1;
            for (int i = firstRowNum; i <= lastRowNum; i++) {
                HSSFRow row = sheetDepartament.getRow(i);
                String stringId = row.getCell(0).toString();
                int id = Integer.parseInt(stringId.substring(0, stringId.indexOf('.')));
                String name = row.getCell(1).toString();
                departaments.add(new Departament(id, name));
            }

            lastRowNum = sheetRoom.getLastRowNum();
            firstRowNum = sheetRoom.getFirstRowNum() + 1;
            for (int i = firstRowNum; i <= lastRowNum; i++) {
                HSSFRow row = sheetRoom.getRow(i);
                String stringId = row.getCell(0).toString();
                String stringCapacity = row.getCell(2).toString();
                int id = Integer.parseInt(stringId.substring(0, stringId.indexOf('.')));
                String name = row.getCell(1).toString();
                int capacity = Integer.parseInt(stringCapacity.substring(0, stringCapacity.indexOf('.')));
                rooms.add(new Room(id, name, capacity));
            }

            lastRowNum = sheetWorker.getLastRowNum();
            firstRowNum = sheetWorker.getFirstRowNum() + 1;
            for (int i = firstRowNum; i <= lastRowNum; i++) {
                HSSFRow row = sheetWorker.getRow(i);
                String stringId = row.getCell(0).toString();
                String stringDep = row.getCell(4).toString();
                String stringPos = row.getCell(5).toString();
                int id = Integer.parseInt(stringId.substring(0, stringId.indexOf('.')));
                String name = row.getCell(1).toString();
                String surname = row.getCell(2).toString();
                String email = row.getCell(3).toString();
                int dep = Integer.parseInt(stringDep.substring(0, stringDep.indexOf('.')));
                int pos = Integer.parseInt(stringDep.substring(0, stringDep.indexOf('.')));
                workers.add(new Worker(id, name, surname, email,
                        Departament.findDepartament(departaments, dep), Worker.Position.getPositionByCode(pos)));
            }

            lastRowNum = sheetEventWorker.getLastRowNum();
            firstRowNum = sheetEventWorker.getFirstRowNum() + 1;

            for (int i = firstRowNum; i <= lastRowNum; i++) {
                String s1 = sheetEventWorker.getRow(i).getCell(0).toString();
                String s2 = sheetEventWorker.getRow(i).getCell(1).toString();
                int eventId = Integer.parseInt(s1.substring(0, s1.indexOf('.')));
                int workerId = Integer.parseInt(s2.substring(0, s2.indexOf('.')));
                ArrayList<Worker> arrayList;
                if (eventWorker.containsKey(eventId)){
                    arrayList = eventWorker.get(eventId);
                }else {
                    arrayList = new ArrayList<>();
                    eventWorker.put(eventId, arrayList);
                }
                arrayList.add(Worker.findWorker(workers, workerId));
            }

            lastRowNum = sheetEvent.getLastRowNum();
            firstRowNum = sheetEvent.getFirstRowNum() + 1;
            for (int i = firstRowNum; i <= lastRowNum; i++) {
                HSSFRow row = sheetEvent.getRow(i);
                String stringId = row.getCell(0).toString();
                String stringRoom = row.getCell(2).toString();
                String stringStart = row.getCell(3).toString();
                String stringEnd = row.getCell(4).toString();
                String stringPrority = row.getCell(5).toString();
                String name = row.getCell(1).toString();
                String note = row.getCell(6).toString();

                int id = Integer.parseInt(stringId.substring(0, stringId.indexOf('.')));
                int room = Integer.parseInt(stringRoom.substring(0, stringRoom.indexOf('.')));
                long start = Long.parseLong(stringStart);
                long end = Long.parseLong(stringEnd);
                int priority = Integer.parseInt(stringPrority.substring(0, stringPrority.indexOf('.')));

                Event.Priority eventPriority = Event.Priority.getPriorityByCode(priority);
                Room roomObject = Room.findRoom(rooms, room);
                ArrayList<Worker> workers1 = eventWorker.get(id);
                if (workers1 == null){
                    workers1 = new ArrayList<>();
                }
                events.add(new Event(id, roomObject, name, start, end, workers1, eventPriority, note));
            }

            DatabaseUtils.deleteAllRecords();

            for (int i = 0; i < departaments.size(); i++) {
                DatabaseUtils.insertDepartament(departaments.get(i));
            }
            for (int i = 0; i < rooms.size(); i++) {
                DatabaseUtils.insertRoom(rooms.get(i));
            }
            for (int i = 0; i < workers.size(); i++) {
                DatabaseUtils.insertWorker(workers.get(i));
            }
            for (int i = 0; i < events.size(); i++) {
                DatabaseUtils.insertEvent(events.get(i));
            }

            myExcelBook.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
