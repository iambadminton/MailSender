package mailsender;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Sanya on 13.12.2018.
 */
public class ListFilesLoader {
    private ArrayList<String> filesArray;
    File folder;

    public void load() {
        for (final File fileEntry : folder.listFiles()) {
            if(fileEntry.isDirectory() == true) {
                continue;
            }
            else {
                this.filesArray.add(fileEntry.getPath());
            }
        }

    }

    public ListFilesLoader(File folder) {
        this.folder = folder;
        this.filesArray = new ArrayList<>();
    }

    public ArrayList<String> getFilesArray() {
        return filesArray;
    }

    public static void main(String[] args) {
        ListFilesLoader loader = new ListFilesLoader(new File(args[0]));
        loader.load();
        ArrayList<String> list = loader.getFilesArray();
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }
}
