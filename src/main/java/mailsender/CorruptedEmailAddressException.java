package mailsender;

/**
 * Created by Sanya on 21.11.2018.
 */
public class CorruptedEmailAddressException extends Exception {
    public CorruptedEmailAddressException(String message) {
        super(message);
    }
}
