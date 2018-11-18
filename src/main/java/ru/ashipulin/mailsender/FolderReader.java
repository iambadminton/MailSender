package ru.ashipulin.mailsender;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by a.shipulin on 06.09.18.
 */
public class FolderReader {
    public static void main(String[] args) throws IOException {
        File fileFolder = new File("D:\\Кино");
        for(final File fileEntry: fileFolder.listFiles())
            if (fileEntry.isFile() == true) {
                /*System.out.println(fileEntry.toString());*/
                Charset charset = Charset.forName("UTF-8");
                BufferedReader content = new BufferedReader(Files.newBufferedReader(Paths.get("C:\\HTMLFolder"), charset));
            }

    }
}
