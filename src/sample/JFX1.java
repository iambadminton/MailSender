package sample;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
            addressbookInput.setText(file.getPath().toString());
        });
        /*root.setCenter(fileChooserButton);*/
        GridPane.setConstraints(fileChooserButton, 2, 0);

        Label directoryLabel = new Label("Папка с файлами");
        GridPane.setConstraints(directoryLabel, 0, 1);

        TextField folderInput = new TextField();
        folderInput.setPromptText("Укажите папку с файлами");
        GridPane.setConstraints(folderInput, 1, 1);

        Button folderChooserButton = new Button("Выбрать папку с файлами");
        folderChooserButton.setOnAction(event -> {
            File file = folderChooser.showDialog(primaryStage);
            folderInput.setText(file.getPath().toString());
        });
        /*root.setCenter(fileChooserButton);*/
        GridPane.setConstraints(folderChooserButton, 2, 1);


        Label themeLabel = new Label("Тема письма");
        GridPane.setConstraints(themeLabel, 0, 2);

        TextField titleInput = new TextField();
        titleInput.setPromptText("Укажите тему письма");
        GridPane.setConstraints(titleInput, 1, 2);

        Button loginButton = new Button("Отправить");
        // loginButton.setOnAction(e -> isNumber(ageInput, ageInput.getText()));


        GridPane.setConstraints(loginButton, 1, 3);

        ProgressBar generalProgressBar = new ProgressBar(0);

        generalProgressBar.setMaxSize(500, 10);
        GridPane.setConstraints(generalProgressBar, 0, 5, 4, 1);
        TextArea log = new TextArea();
        GridPane.setConstraints(log, 0, 6, 4, 1);

        loginButton.setOnAction(e -> {
            try {
                doMailing(addressbookInput, folderInput, titleInput, generalProgressBar, log);
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
        gridPane.getChildren().addAll(addressbookInput, addressbookLabel, folderInput, directoryLabel, loginButton, titleInput, themeLabel, fileChooserButton, generalProgressBar, folderChooserButton, log);

        Scene scene = new Scene(gridPane, 550, 300);

        primaryStage.setResizable(false);
        window.setScene(scene);

        window.show();


    }

    private void doMailing(TextField addressBook, TextField folder, TextField title, ProgressBar bar, TextArea textArea) throws IOException, InvalidFormatException, ParseException, MessagingException {

        bar.setProgress(0);
        AddressBookReader addressBookReader = new AddressBookReader(addressBook.getText());
        addressBookReader.read();
        int count = addressBookReader.list.size();
        int curr = 0;
        for (PersonInfo personInfo : addressBookReader.list) {
            curr++;
            bar.setProgress(curr / count);
            FileFinder fileFinder = new FileFinder(folder.getText());
            //System.out.println(personInfo.getEmail() + fileFinder.getPathByPattern("*" + personInfo.getSecondName() + "*" + personInfo.getFirstName() + "*" + personInfo.getPatronymic() + "*.htm*"));
            String attachedFilePath = fileFinder.getPathByPattern("*" + personInfo.getSecondName() + "*" + personInfo.getFirstName() + "*" + personInfo.getPatronymic() + "*.htm*");
            textArea.appendText("Письмо для " + personInfo.getSecondName() + " " + personInfo.getFirstName() + " " + personInfo.getPatronymic() + " по адресу: " + personInfo.email);
            try {
                if (attachedFilePath != "" && attachedFilePath != null) {
                    //MailSender sender = new MailSender("C:\\SpringProjects\\MailSender\\src\\sample\\mail.properties", personInfo.getEmail(), attachedFilePath, title.getText());
                    MailSender sender = new MailSender("mail.properties", personInfo.getEmail(), attachedFilePath, title.getText());
                    sender.send();
                    textArea.appendText(" Письмо отослано успешно\n");
                } else {
                    textArea.appendText(" Не найден файл в указанной директории.\n ");
                }

            } catch (Exception e) {
                textArea.appendText("Не удалось отправить письмо. " + e.toString());
            }
        }

        textArea.appendText("Отправка писем закончена!");

    }


}
