package mailsender;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.io.*;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Created by Sanya on 08.09.2018.
 */
public class JFX1 extends Application {

    private Stage window;
    private HashMap<String, String> addressbookRowsTypeMap;
    private ImageView imageView;
    private Image trafficLightResultImage;
    private final String IMAGE_FOLDER = "images/";
    private final String RED_IMAGE = "red.png";
    private final String GREEN_IMAGE = "green.png";
    private final String YELLOW_IMAGE = "yellow.png";
    private final String EMPTY_IMAGE = "empty.png";
    private final String SILHOUETTE_IMAGE = "silhouette.png";
    private Date lastDateOfSending;
    private ArrayList<String> sentFiles = new ArrayList<>();
    private ArrayList<String> allFiles;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Отправка расчетных листков");
        GridPane gridPane = new GridPane();
        gridPane.setMinHeight(600);
        gridPane.setMaxHeight(600);
        gridPane.setMinWidth(800);
        gridPane.setMaxWidth(800);
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

        DirectoryChooser folderSaveLogChooser = new DirectoryChooser();
        folderSaveLogChooser.setTitle("Сохранить историю отправки здесь:");
        folderSaveLogChooser.setInitialDirectory(folderSaveLogChooser.getInitialDirectory());


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
        ArrayList allExistFiles = new ArrayList();

        Button folderChooserButton = new Button("Выбрать папку с РЛ");
        folderChooserButton.setOnAction(event -> {
                    File file = folderChooser.showDialog(primaryStage);
                    folderInput.setText(file != null ? file.getPath().toString() : "");
                }
        );

        GridPane.setConstraints(folderChooserButton, 2, 1);

        Button logSaverButton = new Button("Сохранить лог");
        logSaverButton.setOnAction(event -> {
            File file = folderSaveLogChooser.showDialog(primaryStage);
        });


        Label themeLabel = new Label("Тема письма");
        GridPane.setConstraints(themeLabel, 0, 2);

        TextField titleInput = new TextField();
        titleInput.setPromptText("Укажите тему письма");
        GridPane.setConstraints(titleInput, 1, 2);

        Label bodyLabel = new Label("Текст письма");
        GridPane.setConstraints(bodyLabel, 0, 3, 1, 2);

        TextArea bodyInput = new TextArea();
        bodyInput.setPromptText("Текст письма");
        GridPane.setConstraints(bodyInput, 1, 3, 1, 2);

        Button loadTemplateButton = new Button("Загрузить");
        GridPane.setConstraints(loadTemplateButton, 2, 3);


        Button sendButton = new Button("Отправить");
        GridPane.setConstraints(sendButton, 1, 5);

        ProgressBar generalProgressBar = new ProgressBar(0);
        showTrafficLight(EMPTY_IMAGE);
        this.imageView = new ImageView(this.trafficLightResultImage);

        GridPane.setConstraints(imageView, 2, 4);

        generalProgressBar.setMaxSize(700, 10);
        GridPane.setConstraints(generalProgressBar, 0, 6, 4, 1);
        TextArea logTextArea = new TextArea();
        GridPane.setConstraints(logTextArea, 0, 7, 2, 1);

        Button saveLogButton = new Button("Cохранить лог");
        saveLogButton.setDisable(true);
        GridPane.setConstraints(saveLogButton, 2, 4);
        saveLogButton.setOnAction((ActionEvent event) -> {
            FileChooser fileChooser = new FileChooser();
            //Set extension filter
            FileChooser.ExtensionFilter extFilter =
                    new FileChooser.ExtensionFilter("LOG files (*.log)", "*.log");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
            fileChooser.setInitialFileName("Лог_отправки_" + dateFormat.format(this.lastDateOfSending));
            fileChooser.getExtensionFilters().add(extFilter);
            //Show save file dialog
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                if (file.getPath().contains(".log")) {
                    File fileWithExt = new File(file.getPath());
                    saveTemplate(logTextArea.getText().replaceAll("\n", System.getProperty("line.separator")), fileWithExt, logTextArea);
                } else {
                    File fileWithExt = new File(file.getPath() + ".txt");
                    saveTemplate(logTextArea.getText().replaceAll("\n", System.getProperty("line.separator")), fileWithExt, logTextArea);
                }
            }
        });

        Text versionText = new Text("version 1.4");
        GridPane.setConstraints(versionText, 0, 8);
        GridPane.setConstraints(saveLogButton, 2, 7);

        loadTemplateButton.setOnAction((ActionEvent event) -> {
            try {
                FileChooser fileChooser = new FileChooser();
                //Set extension filter
                FileChooser.ExtensionFilter extFilter =
                        new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showOpenDialog(primaryStage);
                try (BufferedReader fileOut = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath()), "Windows-1251"))) {
                    for (String line; (line = fileOut.readLine()) != null; ) {
                        bodyInput.clear();
                        bodyInput.appendText(line + "\n");
                    }
                }
            } catch (IOException e) {
                logTextArea.appendText(e.getMessage());
            } catch (Exception e) {
                logTextArea.appendText(e.getMessage());
            }
        });

        sendButton.setOnAction((ActionEvent e) -> {
            sendButton.setDisable(true);
            saveLogButton.setDisable(true);
            ImageView imageView = this.imageView;
            try {
                this.imageView.setImage(new Image(new File(IMAGE_FOLDER + SILHOUETTE_IMAGE).toURI().toURL().toString()));
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
            Task task = new Task<Void>() {


                @Override
                protected Void call() throws Exception {
                    try {
                        logTextArea.clear();
                        File addressBookFile = new File(addressbookInput.getText());
                        File folder = new File(folderInput.getText());
                        if (addressBookFile.exists() && addressBookFile.isDirectory() == false
                                && folder.exists() && folder.isDirectory() == true) {
                            doMailing(addressbookInput, folderInput, titleInput, bodyInput, generalProgressBar, logTextArea);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY hh:mm");
                            Date now = new Date();
                            saveLogButton.setDisable(false);
                            sendButton.setDisable(false);

                        } else {
                            logTextArea.clear();
                            logTextArea.appendText("Проверьте, правильно ли заполнены поля ");
                            imageView.setImage(new Image(new File(IMAGE_FOLDER + RED_IMAGE).toURI().toURL().toString()));

                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        logTextArea.appendText(e1.getMessage());
                        try {
                            imageView.setImage(new Image(new File(IMAGE_FOLDER + RED_IMAGE).toURI().toURL().toString()));
                        } catch (MalformedURLException e0) {
                            e0.printStackTrace();
                        }
                    } catch (MessagingException e1) {
                        e1.printStackTrace();
                        logTextArea.appendText(e1.getMessage());
                        try {
                            imageView.setImage(new Image(new File(IMAGE_FOLDER + RED_IMAGE).toURI().toURL().toString()));
                        } catch (MalformedURLException e0) {
                            e0.printStackTrace();
                        }
                    }
                    saveLogButton.setDisable(false);
                    sendButton.setDisable(false);
                    return null;
                }

            };

            new Thread(task).start();
            Date now = new Date();
            this.lastDateOfSending = now;

            // загрузим список всех файлов в директории

            try {
                ListFilesLoader curLoader = new ListFilesLoader(new File(folderInput.getText()));
                curLoader.load();
                this.allFiles = curLoader.getFilesArray();
            } catch (Exception e1) {
                logTextArea.appendText("Ошибка чтения файлов в указанной папке: " + e1.getMessage());
            }
        });

        gridPane.getChildren().addAll(addressbookInput, addressbookLabel, folderInput, directoryLabel,
                sendButton, titleInput, themeLabel, fileChooserButton, generalProgressBar, folderChooserButton, logTextArea
                , bodyLabel, bodyInput, this.imageView, saveLogButton, versionText);

        Scene scene = new Scene(gridPane, 800, 600);


        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest((WindowEvent e) -> {
            State state = new State(addressbookInput.getText(), folderInput.getText(), titleInput.getText(), bodyInput.getText());
            try {
                FileOutputStream fileOut = new FileOutputStream("State.ser");
                ObjectOutputStream stream = new ObjectOutputStream(fileOut);
                stream.writeObject(state);
            } catch (IOException ex) {
                logTextArea.appendText(ex.getMessage());
            }

        });

        primaryStage.setOnShowing((WindowEvent event) -> {
            try {
                File file = new File("State.ser");
                if (file.exists()) {
                    FileInputStream stream = new FileInputStream(file);
                    ObjectInputStream inputStream = new ObjectInputStream(stream);
                    State state = (State) inputStream.readObject();
                    inputStream.close();
                    addressbookInput.setText(state.getAddressbookInput());
                    folderInput.setText(state.getFolderInput());
                    titleInput.setText(state.getTitleInput());
                    bodyInput.setText(state.getBodyInput());
                }
            } catch (ClassNotFoundException ex1) {
                logTextArea.appendText(ex1.getMessage());
            } catch (IOException ex) {
                logTextArea.appendText("Ошибка инициализации " + ex.getMessage() + " " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        });

        window.setScene(scene);
        window.show();


    }

    private void doMailing(TextField addressBook, TextField folder, TextField title, TextArea body, ProgressBar bar,
                           TextArea logArea) throws IOException, InvalidFormatException, ParseException, MessagingException {

        int curr = 0;
        int successful = 0;
        String emailFrom;
        String fileExtension;
        bar.setProgress(0);
        ArrayList<HashMap<String, String>> personsInfoArray = new ArrayList<>(); //
        AddressBookReader addressBookReader = new AddressBookReader(addressBook.getText(), personsInfoArray);
        Properties fileProp = new Properties();
        try {
            fileProp.load(new FileInputStream("files.properties"));
        } catch (IOException e) {
            javafx.application.Platform.runLater(() -> logArea.appendText("Ошибка чтения файла files.properties: " + e.getMessage()));
        }
        fileExtension = fileProp.getProperty("file.extension");
        initRowTypeMap(fileProp); // проинициализировали this.addressbookRowsTypeMap из файла file.properties


        addressBookReader.read(this.addressbookRowsTypeMap);
        int count = addressBookReader.addressBook.size();

        //создадим сессию
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("mail.properties"));

        } catch (IOException e) {
            javafx.application.Platform.runLater(() -> logArea.appendText("Ошибка чтения файла mail.properties: " + e.getMessage()));
        }
        emailFrom = properties.getProperty("mail.from");
        Session mailSession = Session.getDefaultInstance(properties);
        Transport tr = mailSession.getTransport();

        tr.connect(properties.getProperty("mail.smtps.user"), properties.getProperty("mail.smtps.password"));

        for (HashMap<String, String> concrtePerson : addressBookReader.addressBook) {
            curr++;
            bar.setProgress((float) curr / count);

            FileFinder fileFinder = new FileFinder(folder.getText());
            String attachedFilePath = fileFinder.getPathByPattern(getDesiredFileName(concrtePerson) + "*." + fileExtension);
            String personEmail = null;
            try {
                personEmail = getPersonEmail(concrtePerson);
            } catch (CorruptedEmailAddressException e) {
                javafx.application.Platform.runLater(() -> logArea.appendText(e.getMessage()));
            }
            String statusInfo = "Письмо для " + getDescribeOfPerson(concrtePerson, " ") + " по адресу: " + personEmail + ":  ";
            try {
                if (attachedFilePath != "" && attachedFilePath != null) {
                    MailSender sender = new MailSender(tr, mailSession, emailFrom, getPersonEmail(concrtePerson), attachedFilePath, title.getText(), body.getText(), logArea);
                    sender.send();
                    javafx.application.Platform.runLater(() -> logArea.appendText(statusInfo + "Письмо отослано успешно\n"));
                    successful++;
                    this.sentFiles.add(attachedFilePath);
                } else {
                    javafx.application.Platform.runLater(() -> logArea.appendText(statusInfo + "!!! Не найден файл в указанной директории. Письмо не отправлено\n"));
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> logArea.appendText(statusInfo + "!!! Не удалось отправить письмо. " + e.toString() + "\n"));
            }
        }
        tr.close();

        // выведем ошибки чтения адресной книги в лог
        for (int ii = 0; ii < addressBookReader.errors.size(); ii++) {
            int errorNumber = ii;
            javafx.application.Platform.runLater(() -> logArea.appendText(addressBookReader.errors.get(errorNumber) + "\n"));
        }

        HashSet<String> allFilesSet = new HashSet<String>(this.allFiles);
        allFilesSet.removeAll(this.sentFiles);

        for (String fileStr : allFilesSet) {
            javafx.application.Platform.runLater(() -> logArea.appendText("Не отправлен файл(не указан адресат) " + fileStr.substring(fileStr.lastIndexOf(File.separator) + 1) + "\n"));
        }

        //logArea.appendText("Отправка писем закончена!\n");
        int finalSuccessful = successful;
        javafx.application.Platform.runLater(() -> {
            logArea.appendText("Отправка писем закончена! Отослано успешно " + finalSuccessful + " писем из " + count + " адресов\n");
            if (allFilesSet.size() == 0) {
                logArea.appendText("Файлов в выбранной папке, для которых не был указан адресат, не найдено.\n");
            } else {
                logArea.appendText("Файлов в выбранной папке, для которых не был указан адресат, всего: " + allFilesSet.size() + " шт.\n");
            }
        });


        String imageName = getVisualSignal(finalSuccessful, allFilesSet.size(), count);
        this.imageView.setImage(new Image(new File(IMAGE_FOLDER + imageName).toURI().toURL().toString()));


    }

    private void saveTemplate(String content, File file, TextArea logArea) {
        try {
            FileWriter fileWriter;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException ex) {
            logArea.appendText(ex.getMessage());
        }

    }

    private void initRowTypeMap(Properties properties) {
        Map<String, String> map = properties.entrySet().stream().collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
        Map<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile("excel.field[0-9]+.type");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Matcher matcher = pattern.matcher(entry.getKey());
            if (matcher.matches()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        this.addressbookRowsTypeMap = (HashMap<String, String>) result;
    }

    private String getDesiredFileName(HashMap<String, String> personInfo) {
        String result = getDescribeOfPerson(personInfo, "_");
        return result;
    }

    private String getPersonEmail(HashMap<String, String> personInfo) throws CorruptedEmailAddressException {
        String result = new String();
        boolean isValid;
        EmailValidator validator = new EmailValidator();
        TreeMap<String, String> sortedMap = new TreeMap<>(personInfo);
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            result = entry.getValue();
        }
        isValid = validator.validate(result);
        if (isValid == false) {
            throw new CorruptedEmailAddressException("Ошибка: " + result + " не является валидным email-адресом. Необходимо исправить данные в адресной книге.");
        }
        return result;
    }

    private String getDescribeOfPerson(HashMap<String, String> personInfo, String delimiter) {
        int lastPos = 0;
        TreeMap<String, String> sortedMap = new TreeMap<String, String>(personInfo);
        StringBuilder string = new StringBuilder();
        String lastKey = "";
        //очистим отсортированную мапу от последнего элемента - это email, он нам не нужен в описании
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            lastKey = entry.getKey();
        }
        sortedMap.remove(lastKey);

        // оставшаяся отсортированная мапа не содержит email и пригодна для формирования инфы о персоне
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            string.append(entry.getValue());
            string.append(delimiter);
        }
        lastPos = string.lastIndexOf(delimiter);
        string.deleteCharAt(lastPos);
        return string.toString();
    }

    public void showTrafficLight(String image) {

        try {
            this.trafficLightResultImage = new Image(new File(IMAGE_FOLDER + image).toURI().toURL().toString());
            this.imageView = new ImageView(trafficLightResultImage);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getVisualSignal(int sentCount, int notSentCount, int allEcxelItemsCount) {
        String imageName = "";
        if (sentCount == 0) {
            imageName = RED_IMAGE;

        } else if (sentCount == allEcxelItemsCount && notSentCount == 0) {
            imageName = GREEN_IMAGE;
        } else {
            imageName = YELLOW_IMAGE;
        }
        return imageName;
    }


}
