package sample;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import java.io.*;
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
        /*GridPane.setConstraints(bodyLabel, 0, 3);*/
        GridPane.setConstraints(bodyLabel, 0, 3, 1, 2);


        TextArea bodyInput = new TextArea();
        bodyInput.setPromptText("Текст письма");
        //GridPane.setConstraints(bodyInput, 1, 3, 2, 1);
        GridPane.setConstraints(bodyInput, 1, 3, 1, 2);


        /*Image loadImage = new Image(getClass().getResourceAsStream("/LOAD.png"));
        Button loadTemplateButton = new Button("Загрузить", new ImageView(loadImage));*/
        Button loadTemplateButton = new Button("Загрузить");
        GridPane.setConstraints(loadTemplateButton, 2, 3);



        Button sendButton = new Button("Отправить");
        // sendButton.setOnAction(e -> isNumber(ageInput, ageInput.getText()));
        GridPane.setConstraints(sendButton, 1, 5);

        ProgressBar generalProgressBar = new ProgressBar(0);

        generalProgressBar.setMaxSize(500, 10);
        GridPane.setConstraints(generalProgressBar, 0, 6, 4, 1);
        TextArea log = new TextArea();
        GridPane.setConstraints(log, 0, 7, 4, 1);


        Button saveTemplateButton = new Button("Cохранить");
        GridPane.setConstraints(saveTemplateButton, 2, 4);
        saveTemplateButton.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            //Set extension filter
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
            //Show save file dialog
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                if(file.getPath().contains(".txt")){
                    File fileWithExt = new File(file.getPath());
                    SaveTemplate(bodyInput.getText(), fileWithExt, log);
                }
                else {
                    File fileWithExt = new File(file.getPath() + ".txt");
                    SaveTemplate(bodyInput.getText(), fileWithExt, log);
                }
            }
        });


        loadTemplateButton.setOnAction((ActionEvent event) -> {
            try {
                FileChooser fileChooser = new FileChooser();
                //Set extension filter
                FileChooser.ExtensionFilter extFilter =
                        new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
                fileChooser.getExtensionFilters().add(extFilter);
                //Show save file dialog
                //File file = fileChooser.showSaveDialog(primaryStage);
                File file = fileChooser.showOpenDialog(primaryStage);
                try (BufferedReader fileOut = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath()), "Windows-1251"))) {
                    for (String line; (line = fileOut.readLine()) != null; ) {
                        //System.out.println(line);
                        bodyInput.clear();
                        bodyInput.appendText(line+"\n");
                    }
                }
            /*if(file != null){
                SaveTemplate(log.getText(), fileWithExt, log);
            }*/
            } catch (IOException e) {
                log.appendText(e.getMessage());
            } catch (Exception e) {
                log.appendText(e.getMessage());
            }
        });


        sendButton.setOnAction((ActionEvent e) -> {
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        log.clear();
                        File addressBookFile = new File(addressbookInput.getText());
                        File folder = new File(folderInput.getText());
                        if (addressBookFile.exists() && addressBookFile.isDirectory() == false
                                && folder.exists() && folder.isDirectory()==true) {
                            doMailing(addressbookInput, folderInput, titleInput, bodyInput, generalProgressBar, log);
                        }
                        else {
                            log.clear();
                            log.appendText("Проверьте, правильно ли заполнены поля ");
                        }
                    } catch (IOException e1)

                    {
                        e1.printStackTrace();
                        log.appendText(e1.getMessage());
                    } catch (MessagingException e1) {
                        e1.printStackTrace();
                        log.appendText(e1.getMessage());
                    }
                    return null;
                }

            };
            new Thread(task).start();
        });
        /*gridPane.getChildren().addAll(addressbookInput, addressbookLabel, folderInput, directoryLabel,
                sendButton, titleInput, themeLabel, fileChooserButton, generalProgressBar, folderChooserButton, log
                , bodyLabel, bodyInput);*/
        gridPane.getChildren().addAll(addressbookInput, addressbookLabel, folderInput, directoryLabel,
                sendButton, titleInput, themeLabel, fileChooserButton, generalProgressBar, folderChooserButton, log
                , bodyLabel, bodyInput, loadTemplateButton, saveTemplateButton);

        Scene scene = new Scene(gridPane, 700, 600);
        primaryStage.setResizable(false);
        window.setScene(scene);
        window.show();


    }

    private void doMailing(TextField addressBook, TextField folder, TextField title, TextArea body, ProgressBar bar,
                           TextArea logArea) throws IOException, InvalidFormatException, ParseException, MessagingException {

        bar.setProgress(0);
        AddressBookReader addressBookReader = new AddressBookReader(addressBook.getText());
        addressBookReader.read();
        int count = addressBookReader.list.size();
        int curr = 0;
        String emailFrom;
        String fileExtension;

        Properties fileProp = new Properties();
        fileProp.load(new FileInputStream("file.properties"));
        //fileProp.load(new FileInputStream("C:\\SpringProjects\\MailSender\\out\\artifacts\\MailSender_jar\\file.properties"));
        fileExtension = fileProp.getProperty("file.extension");
        /*if (fileExtension == null || fileExtension == "") {
            System.out.println("fileExtension = null");
        } else {
            System.out.println("== " + fileExtension);
        }*/


        //создадим сессию
        Properties properties = new Properties();
        properties.load(new FileInputStream("mail.properties")); //потом раскомментировать
        // properties.load(new FileInputStream("C:\\SpringProjects\\MailSender\\out\\artifacts\\MailSender_jar\\mail.properties"));
        emailFrom = properties.getProperty("mail.from");
        Session mailSession = Session.getDefaultInstance(properties);
        Transport tr = mailSession.getTransport();
        tr.connect(properties.getProperty("mail.smtps.user"), properties.getProperty("mail.smtps.password"));

        for (PersonInfo personInfo : addressBookReader.list) {
            curr++;
            bar.setProgress((float) curr / count);

            FileFinder fileFinder = new FileFinder(folder.getText());
            String attachedFilePath = fileFinder.getPathByPattern("*" + personInfo.getSecondName() + "*" + personInfo.getFirstName() + "*" + personInfo.getPatronymic() + "*." + fileExtension);
            /*System.out.println("attachedFilePath=" + attachedFilePath);*/
            //logArea.appendText("Письмо для " + personInfo.getSecondName() + " " + personInfo.getFirstName() + " " + personInfo.getPatronymic() + " по адресу: " + personInfo.email + "\n");
            String statusInfo = new String("Письмо для " + personInfo.getSecondName() + " " + personInfo.getFirstName() + " " + personInfo.getPatronymic() + " по адресу: " + personInfo.email + "\n");

            try {
                if (attachedFilePath != "" && attachedFilePath != null) {
                    MailSender sender = new MailSender(tr, mailSession, emailFrom, personInfo.getEmail(), attachedFilePath, title.getText(), body.getText(), logArea);
                    sender.send();
                    //logArea.appendText(" Письмо отослано успешно\n");
                    javafx.application.Platform.runLater(() -> logArea.appendText(statusInfo + "Письмо отослано успешно\n"));
                } else {
                    //logArea.appendText(" Не найден файл в указанной директории.\n ");
                    javafx.application.Platform.runLater(() -> logArea.appendText(statusInfo + "Не найден файл в указанной директории. Письмо не отправлено\n"));
                }
            } catch (Exception e) {
                //logArea.appendText("Не удалось отправить письмо. " + e.toString() + "\n");
                javafx.application.Platform.runLater(() -> logArea.appendText(statusInfo + "Не удалось отправить письмо. " + e.toString() + "\n"));
            }
        }
        tr.close();
        //logArea.appendText("Отправка писем закончена!\n");
        javafx.application.Platform.runLater(() -> logArea.appendText("Отправка писем закончена!\n"));

    }

    private void SaveTemplate(String content, File file, TextArea logArea) {
        try {
            FileWriter fileWriter;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException ex) {
            logArea.appendText(ex.getMessage());
        }

    }


}
