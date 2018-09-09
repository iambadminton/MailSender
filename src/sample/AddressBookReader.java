package sample;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by a.shipulin on 06.09.18.
 */
public class AddressBookReader {
    String path;
    ArrayList<PersonInfo> list;

    public AddressBookReader(String path) {
        this.path = path;
    }

    public void read() throws IOException, InvalidFormatException, ParseException {
        String version = checkVersion();
        if (version.equals("OLE2")) {
            readXLS();
        } else if (version.equals("OOXML")) {
            readXLSX();
        }
        else {
            throw new IOException("Адресная книга имеет неподдерживаемый формат.");
        }
    }

    public void readXLS() throws IOException {
        ArrayList<PersonInfo> list = new ArrayList<>();
        PersonInfo personInfo;
        InputStream inputStream = new FileInputStream(this.path);
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        HSSFSheet sheet = workbook.getSheetAt(0);
        String secondName = "";
        String firstName = "";
        String patronymic = "";
        String birthday = "";
        String email = "";

        Iterator<Row> rowIterator = sheet.rowIterator();
        int rows = sheet.getPhysicalNumberOfRows();
        for (int r = 0; r < rows; r++) {
            HSSFRow row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = 0; c < row.getLastCellNum(); c++) {
                HSSFCell cell = row.getCell(c);
                if (cell != null) {
                    cell.setCellType(CellType.STRING);
                    if (c == 0) {
                        secondName = (String) cell.getStringCellValue();
                    }
                    if (c == 1) {
                        firstName = (String) cell.getStringCellValue();
                    }
                    if (c == 2) {
                        patronymic = (String) cell.getStringCellValue();
                    }
                    if (c == 3) {
                        birthday = (String) cell.getStringCellValue();
                    }
                    if (c == 4) {
                        email = (String) cell.getStringCellValue();
                    }

                }

            }
            personInfo = new PersonInfo(secondName, firstName, patronymic, birthday, email);
            list.add(personInfo);
        }
        this.list = list;

    }


    public void readXLSXRows(Iterator<Row> rowIterator) throws ParseException {
        PersonInfo personInfo;
        ArrayList<PersonInfo> list = new ArrayList<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            String secondName = "";
            String firstName = "";
            String patronymic = "";
            String birthday = "";
            String email = "";
            Date birthdayDate = new Date();
            String cur_value;
            int j = 0;
            while (cellIterator.hasNext()) {
                j++;
                Cell cell = cellIterator.next();
                cell.setCellType(CellType.STRING);
                /*if (j == 1) {
                    fio = (String) cell.getStringCellValue();
                }
                if (j == 2) {
                    email = (String) cell.getStringCellValue();
                }
                if (j == 3) {
                    id = (String) cell.getStringCellValue();
                }*/
                if (j == 1) {
                    secondName = (String) cell.getStringCellValue();
                }
                if (j == 2) {
                    firstName = (String) cell.getStringCellValue();
                }
                if (j == 3) {
                    patronymic = (String) cell.getStringCellValue();
                }
                if (j == 4) {
                    birthday = cell.getStringCellValue();
                }
                if (j == 5) {
                    email = (String) cell.getStringCellValue();
                }
            }
            personInfo = new PersonInfo(secondName, firstName, patronymic, birthday, email);
            list.add(personInfo);

        }
        this.list = list;

    }


    public void readXLSX() throws IOException, InvalidFormatException, ParseException {
        ArrayList<PersonInfo> list = new ArrayList<>();

        InputStream inputStream = new FileInputStream(this.path);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        readXLSXRows(rowIterator);

    }

    public String checkVersion() throws IOException {
        FileMagic fileMagic = FileMagic.valueOf(new BufferedInputStream(new FileInputStream(this.path)));

        if (fileMagic.equals(FileMagic.OLE2)) {
            return "OLE2";
        } else if (fileMagic.equals(FileMagic.OOXML)) {
            return "OOXML";
        } else {
            throw new IOException("Адресная книга имеет неподдерживаемый формат.");
        }
    }

    public static void main(String[] args) throws InvalidFormatException, IOException {
        //AddressBookReader reader = new AddressBookReader("C:\\Documents and Settings\\a.shipulin\\Рабочий стол\\Сотрудники ЦКБ РАН.xlsx");
        AddressBookReader reader = new AddressBookReader("D:\\MailSender\\Адресная книга.xlsx");
        reader.checkVersion();
        try {
            reader.read();
            /*if (reader.list == null) {
                System.out.println("========= NULL +++++++++++");
            }
            for (PersonInfo info : reader.list) {
                System.out.println(info.getSecondName() + " " + info.getFirstName() + " " + info.getPatronymic() + " " + info.birthday + " " + info.getEmail());
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
