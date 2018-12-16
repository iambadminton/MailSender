package mailsender;

import java.io.Serializable;

/**
 * Created by Sanya on 13.09.2018.
 */
public class State implements Serializable {
    String addressbookInput;
    String folderInput;
    String titleInput;
    String bodyInput;

    public State(String addressbookInput, String folderInput, String titleInput, String bodyInput) {
        this.addressbookInput = addressbookInput;
        this.folderInput = folderInput;
        this.titleInput = titleInput;
        this.bodyInput = bodyInput;
    }

    public String getAddressbookInput() {
        return addressbookInput;
    }

    public String getFolderInput() {
        return folderInput;
    }

    public String getTitleInput() {
        return titleInput;
    }

    public String getBodyInput() {
        return bodyInput;
    }

}
