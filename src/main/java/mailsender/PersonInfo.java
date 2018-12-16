package mailsender;

/**
 * Created by a.shipulin on 06.09.18.
 */
public class PersonInfo {
    String secondName;
    String firstName;
    String patronymic;
    String birthday;
    String email;

    public PersonInfo(String secondName, String firstName, String patronymic, String birthday, String email) {
        this.secondName = secondName;
        this.firstName = firstName;
        this.patronymic = patronymic;
        this.birthday = birthday;
        this.email = email;
    }

    public String getSecondName() {
        return secondName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }
}
