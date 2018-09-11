package sample;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;


/**
 * Created by Sanya on 08.09.2018.
 */
public class JFX1 extends Application {
    Stage window;

    public static void main(String[] args) {
        /*Properties properties = new Properties();
       try {
            properties.load(new FileInputStream("mail.properties"));
            System.out.println(properties.getProperty("mail.from"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Отправка расчетных листков");
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(12);

        Label addressbookLabel = new Label("Адресная книга");
        GridPane.setConstraints(addressbookLabel, 0, 0);
        TextField addressbookInput = new TextField("Укажите адресную книгу");
        addressbookInput.setPrefWidth(400);
        GridPane.setConstraints(addressbookInput, 1, 0);
        addressbookInput.setEditable(false);

        FileChooser addressbookChooser = new FileChooser();
        addressbookChooser.setTitle("Открыть папку");
        addressbookChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel files", "*.xlsx"),
                new FileChooser.ExtensionFilter("Excel files 97-2003", "*.xls"));
        //File addressBook = addressbookChooser.

        DirectoryChooser folderChooser = new DirectoryChooser();
        folderChooser.setTitle("Выбрать папку для загрузки");
        folderChooser.setInitialDirectory(folderChooser.getInitialDirectory());

        Button fileChooserButton = new Button("Выбрать файл");
        fileChooserButton.setOnAction(event -> {
            File file = addressbookChooser.showOpenDialog(primaryStage);
            addressbookInput.setText(file != null ? file.getPath().toString() : "");
        });
        /*root.setCenter(fileChooserButton);*/
        GridPane.setConstraints(fileChooserButton, 2, 0);

        Label directoryLabel = new Label("Папка с РЛ");
        GridPane.setConstraints(directoryLabel, 0, 1);

        TextField folderInput = new TextField();
        folderInput.setPromptText("Укажите папку с РЛ");
        GridPane.setConstraints(folderInput, 1, 1);
        folderInput.setEditable(false);

        Button folderChooserButton = new Button("Выбрать папку с РЛ");
        folderChooserButton.setOnAction(event -> {
            File file = folderChooser.showDialog(primaryStage);
            folderInput.setText(file != null ? file.getPath().toString() : "");
        });
        /*root.setCenter(fileChooserButton);*/
        GridPane.setConstraints(folderChooserButton, 2, 1);


        Label themeLabel = new Label("Тема письма");
        GridPane.setConstraints(themeLabel, 0, 2);

        TextField titleInput = new TextField();
        titleInput.setPromptText("Укажите тему письма");
        GridPane.setConstraints(titleInput, 1, 2);

        Label bodyLabel = new Label("Текст письма");
        GridPane.setConstraints(bodyLabel, 0, 3);

        TextArea bodyInput = new TextArea();
        bodyInput.setPromptText("Текст письма");
        GridPane.setConstraints(bodyInput, 1, 3, 2, 1);


        Button sendButton = new Button("Отправить");
        // sendButton.setOnAction(e -> isNumber(ageInput, ageInput.getText()));
        GridPane.setConstraints(sendButton, 1, 4);

        ProgressBar generalProgressBar = new ProgressBar(0);

        generalProgressBar.setMaxSize(500, 10);
        GridPane.setConstraints(generalProgressBar, 0, 5, 4, 1);
        TextArea log = new TextArea();
        GridPane.setConstraints(log, 0, 6, 4, 1);

        sendButton.setOnAction(e -> {
            try {
                log.clear();
                File addressBookFile = new File(addressbookInput.getText());
                File folder = new File(folderInput.getText());
                if (addressBookFile != null && folder != null) {
                    doMailing(addressbookInput, folderInput, titleInput, bodyInput, generalProgressBar, log);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (InvalidFormatException e1) {
                e1.printStackTrace();
            } catch (ParseException e1) {
                e1.printStackTrace();
            } catch (MessagingException e1) {
                e1.printStackTrace();
            }
        });
        gridPane.getChildren().addAll(addressbookInput, addressbookLabel, folderInput, directoryLabel,
                sendButton, titleInput, themeLabel, fileChooserButton, generalProgressBar, folderChooserButton, log
                , bodyLabel, bodyInput);

        Scene scene = new Scene(gridPane, 700, 700);

        primaryStage.setResizable(false);
        window.setScene(scene);

        window.show();


    }

    private void doMailing(TextField addressBook, TextField folder, TextField title, TextArea body, ProgressBar bar, TextArea logArea) throws IOException, InvalidFormatException, ParseException, MessagingException {

        bar.setProgress(0);
        AddressBookReader addressBookReader = new AddressBookReader(addressBook.getText());
        addressBookReader.read();
        int count = addressBookReader.list.size();
        int curr = 0;
        String emailFrom;
        String fileExtension;

        Properties fileProp = new Properties();
        fileProp.load(new FileInputStream("C:\\SpringProjects\\MailSender\\src\\sample\\file.properties"));
        fileExtension = fileProp.getProperty("file.extension");
        if (fileExtension == null || fileExtension == "") {
            System.out.println("fileExtension = null");
        } else {
            System.out.println("== " + fileExtension);
        }


        //создадим сессию
        Properties properties = new Properties();
        //properties.load(new FileInputStream("mail.properties")); //потом раскомментировать
        properties.load(new FileInputStream("C:\\SpringProjects\\MailSender\\src\\sample\\mail.properties"));
        emailFrom = properties.getProperty("mail.from");
        Session mailSession = Session.getDefaultInstance(properties);
        Transport tr = mailSession.getTransport();
        tr.connect(properties.getProperty("mail.smtps.user"), properties.getProperty("mail.smtps.password"));
        /*tr.sendMessage(message, message.getAllRecipients());
        */


        for (PersonInfo personInfo : addressBookReader.list) {
            curr++;
            bar.setProgress(curr / count);
            FileFinder fileFinder = new FileFinder(folder.getText());
            //System.out.println(personInfo.getEmail() + fileFinder.getPathByPattern("*" + personInfo.getSecondName() + "*" + personInfo.getFirstName() + "*" + personInfo.getPatronymic() + "*.htm*"));
            String attachedFilePath = fileFinder.getPathByPattern("*" + personInfo.getSecondName() + "*" + personInfo.getFirstName() + "*" + personInfo.getPatronymic() + "*." + fileExtension);
            logArea.appendText("Письмо для " + personInfo.getSecondName() + " " + personInfo.getFirstName() + " " + personInfo.getPatronymic() + " по адресу: " + personInfo.email + "\n");


            try {
                if (attachedFilePath != "" && attachedFilePath != null) {
                    MailSender sender = new MailSender(tr, mailSession, emailFrom, personInfo.getEmail(), attachedFilePath, title.getText(), body.getText());
                    sender.send();
                    logArea.appendText(" Письмо отослано успешно\n");
                } else {
                    logArea.appendText(" Не найден файл в указанной директории.\n ");
                }
            } catch (Exception e) {
                logArea.appendText("Не удалось отправить письмо. " + e.toString() +"\n");
            }
        }
        tr.close();
        logArea.appendText("Отправка писем закончена!\n");

    }


}
