package sample;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Created by Sanya on 09.09.2018.
 */
public class FileFinder {

    String dir;

    public FileFinder(String dir) {
        this.dir = dir;
    }

    public String getPathByPattern(String pattern) {
        File dir = new File(this.dir);
        FileFilter fileFilter = new WildcardFileFilter(pattern);
        File[] files = dir.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++) {
            return(files[i].getPath());
        }
        return null;

    }

    public static void main(String[] args) {
        File dir = new File("D:\\Расчетные листки");
        FileFilter fileFilter = new WildcardFileFilter("Иванов*Иван*Иванович*.htm*");
        File[] files = dir.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i]);
        }
    }
}
