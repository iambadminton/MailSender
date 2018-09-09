package sample;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import jdk.internal.org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by a.shipulin on 05.09.18.
 */
public class MailSender {
    String propertieFile;
    String email;
    String attachedFilePath;
    String title;
    private ArrayList<PersonInfo> list;

    public MailSender(String propertieFile, String email, String attachedFilePath, String title) {
        this.propertieFile = propertieFile;
        this.email = email;
        this.attachedFilePath = attachedFilePath;
        this.title = title;
    }

    public void send() throws IOException, MessagingException {
        final Properties properties = new Properties();


        //properties.load(new FileInputStream("mail.properties"));
        properties.load(new FileInputStream(this.propertieFile));

        Session mailSession = Session.getDefaultInstance(properties);

        System.out.println(mailSession.getProperties());
        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(new InternetAddress(properties.getProperty("mail.from")));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(this.email));
        message.setSubject(this.title);

        Multipart multipart = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();


        javax.activation.DataSource source = new FileDataSource(this.attachedFilePath);
        Path p = Paths.get(this.attachedFilePath);
        String file = p.getFileName().toString();

        //messageBodyPart.setFileName(MimeUtility.encodeText("Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm", "UTF-8", null));
        messageBodyPart.setFileName(MimeUtility.encodeText(file, "UTF-8", null));

        messageBodyPart.setDataHandler(new DataHandler(source));
        multipart.addBodyPart(messageBodyPart);
        MimeBodyPart messageBodyPart2 = new MimeBodyPart();

        /*messageBodyPart2.setText("Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm", "UTF-8", "html");
        messageBodyPart2.setContent("Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm", "text/html; charset=utf-8");*/
        messageBodyPart2.setText(file, "UTF-8", "html");
        messageBodyPart2.setContent(file, "text/html; charset=utf-8");

        multipart.addBodyPart(messageBodyPart2);
        message.setContent(multipart);

        Transport tr = mailSession.getTransport();
        System.out.println(tr.getURLName());
        tr.connect(properties.getProperty("mail.smtps.user"), properties.getProperty("mail.smtps.password"));
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException,
            XMLStreamException, TransformerException, MessagingException {
        final Properties properties = new Properties();

       /* //properties.load(new FileInputStream("mail.properties"));
        properties.load(new FileInputStream("C:\\SpringProjects\\MailSender\\src\\sample\\mail.properties"));

        Session mailSession = Session.getDefaultInstance(properties);

        System.out.println(mailSession.getProperties());
        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(new InternetAddress("shsanya@yandex.ru"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("shsanya@inbox.ru"));
        message.setSubject("Расчетный листок за август");

        Multipart multipart = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();


        javax.activation.DataSource source = new FileDataSource("D:\\Расчетные листки\\Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm");
        messageBodyPart.setFileName(MimeUtility.encodeText("Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm", "UTF-8", null));

        messageBodyPart.setDataHandler(new DataHandler(source));
        multipart.addBodyPart(messageBodyPart);
        MimeBodyPart messageBodyPart2 = new MimeBodyPart();

        messageBodyPart2.setText("Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm", "UTF-8", "html");
        messageBodyPart2.setContent("Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm", "text/html; charset=utf-8");

        multipart.addBodyPart(messageBodyPart2);
        message.setContent(multipart);

        Transport tr = mailSession.getTransport();
        System.out.println(tr.getURLName());
        tr.connect("shsanya", properties.getProperty("mail.smtps.password"));
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();*/

       MailSender sender = new MailSender("C:\\SpringProjects\\MailSender\\src\\sample\\mail.properties", "shsanya@inbox.ru",
               "D:\\Расчетные листки\\Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm", "test");
       sender.send();

    }


}
