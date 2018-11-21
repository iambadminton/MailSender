package ru.ashipulin.mailsender;

import javafx.scene.control.TextArea;
import jdk.internal.org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by a.shipulin on 05.09.18.
 */
public class MailSender {
    Transport transport;
    Session mailSession;
    String emailFrom;
    String emailTo;
    String attachedFilePath;
    String title;
    String body;
    private ArrayList<PersonInfo> list;

    public MailSender(Transport transport, Session mailSession, String emailFrom, String emailTo, String attachedFilePath,
                      String title, String body, TextArea logArea) {
        this.transport = transport;
        this.mailSession = mailSession;
        this.emailFrom = emailFrom;
        this.emailTo = emailTo;
        this.attachedFilePath = attachedFilePath;
        this.title = title;
        this.body = body;
    }

    public void send() throws IOException, MessagingException {
        final Properties properties = new Properties();


        //properties.load(new FileInputStream("mail.properties"));
        MimeMessage message = new MimeMessage(this.mailSession);

        //message.setFrom(new InternetAddress(properties.getProperty("mail.from")));
        message.setFrom(new InternetAddress(this.emailFrom));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(this.emailTo));
        message.setSubject(this.title);

        Multipart multipart = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();


        javax.activation.DataSource source = new FileDataSource(this.attachedFilePath);
        Path p = Paths.get(this.attachedFilePath);
        String file = p.getFileName().toString();

        //messageBodyPart.setFileName(MimeUtility.encodeText("Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm", "UTF-8", null));
        messageBodyPart.setFileName(MimeUtility.encodeText(file, "UTF-8", null));
        messageBodyPart.setText(this.body);

        messageBodyPart.setDataHandler(new DataHandler(source));
        multipart.addBodyPart(messageBodyPart);
        MimeBodyPart messageBodyPart2 = new MimeBodyPart();

        //messageBodyPart2.setText(file, "UTF-8", "html"); // =======
        messageBodyPart2.setText(this.body, "UTF-8", "html");
        messageBodyPart2.setContent(this.body, "text/html; charset=utf-8");
        multipart.addBodyPart(messageBodyPart2);
        message.setContent(multipart);

        /*Transport tr = mailSession.getTransport();
        System.out.println(tr.getURLName());
        tr.connect(properties.getProperty("mail.smtps.user"), properties.getProperty("mail.smtps.password"));
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();*/
        this.transport.sendMessage(message, message.getAllRecipients());
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException,
            XMLStreamException, TransformerException, MessagingException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("mail.properties")); //потом раскомментировать
        Session mailSession = Session.getDefaultInstance(properties);
        Transport tr = mailSession.getTransport();
        tr.connect(properties.getProperty("mail.smtps.user"), properties.getProperty("mail.smtps.password"));




       /*MailSender sender = new MailSender(tr, mailSession, "shsanya@yandex.ru", "shsanya@inbox.ru",
               "D:\\08092018\\Иванов_Иван_Иванович  _01.01.1980_РЛ_за_09_2018.htm", "тема", "боди");
       sender.send();*/

    }


}
