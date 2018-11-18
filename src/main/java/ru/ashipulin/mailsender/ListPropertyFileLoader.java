package ru.ashipulin.mailsender;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Sanya on 27.10.2018.
 */
public class ListPropertyFileLoader {
    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            FileInputStream stream = new FileInputStream("C:\\SpringProjects\\MailSender\\out\\artifacts\\MailSender_jar\\file.properties");
            properties.load(stream);
            stream.close();
            System.out.println(properties.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
