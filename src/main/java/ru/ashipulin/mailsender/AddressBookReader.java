package ru.ashipulin.mailsender;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by a.shipulin on 06.09.18.
 */
public class AddressBookReader {
    String path;
    //ArrayList<PersonInfo> list;
    ArrayList<HashMap<String, String>> list;


    public AddressBookReader(String path, ArrayList<HashMap<String, String>> list) {
        this.path = path;
    }

    public void read(HashMap<String, String> rowsTypeMap) throws IOException, InvalidFormatException, ParseException {
        String version = checkVersion();
        /*if (version.equals("OLE2")) {
            readXLS(rowsTypeMap);
        } else*/
        if (version.equals("OOXML")) {
            readXLSX(rowsTypeMap);
        } else {
            throw new IOException("Адресная книга имеет неподдерживаемый формат.");
        }
    }

    public void readXLS(HashMap<String, String> rowsTypeMap) throws IOException {
        // @TODO: переписать для List<HashMap<String, String>>
        /*//ArrayList<PersonInfo> list = new ArrayList<>();
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
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
        this.list = list;*/

    }


    public void readXLSXRows(Iterator<Row> rowIterator, HashMap<String, String> rowsTypeMap) throws ParseException {
        //PersonInfo personInfo;
        //ArrayList<PersonInfo> list = new ArrayList<>();
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        HashMap<String, String> person;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            person = new HashMap<String, String>();
            Date birthdayDate = new Date();
            String cur_value;
            int j = 0;
            while (cellIterator.hasNext()) {
                j++;
                String fieldNumStr = "field" + j;
                Cell cell = cellIterator.next();
                String fieldNumType = rowsTypeMap.get("excel." + fieldNumStr + ".type");
                if (fieldNumType.equalsIgnoreCase("date")) {
                    DataFormatter dataFormatter = new DataFormatter();
                    dataFormatter.formatCellValue(cell);
                    Double birthday = cell.getNumericCellValue();
                    //Integer birthDayInt = Integer.parseInt(birthday);

                    Date javaDate = DateUtil.getJavaDate(birthday);
                    //System.out.println(new SimpleDateFormat("dd.MM.yyyy").format(javaDate));
                    String dateStr = new SimpleDateFormat("dd.MM.yyyy").format(javaDate);
                    person.put(fieldNumStr, dateStr);
                } else if (fieldNumType.equalsIgnoreCase("string")) {
                    String valueString = (String) cell.getStringCellValue();
                    person.put(fieldNumStr, valueString);
                } else if (fieldNumType.equalsIgnoreCase("number")) {
                    Double valueDouble = (Double) cell.getNumericCellValue();
                    person.put(fieldNumStr, valueDouble.toString());
                }
            }
            list.add(person);
        }
        this.list = list;
    }


    public void readXLSX(HashMap<String, String> rowsTypeMap) throws IOException, InvalidFormatException, ParseException {
        ArrayList<PersonInfo> list = new ArrayList<>();

        InputStream inputStream = new FileInputStream(this.path);
        Workbook workbook = new XSSFWorkbook(inputStream);
        Iterator<Sheet> sheetIterator = workbook.sheetIterator();
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        readXLSXRows(rowIterator, rowsTypeMap);

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
        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
        AddressBookReader reader = new AddressBookReader("Адресная книга - копия.xlsx", arrayList);
        HashMap<String, String> predefinedRowsTypeMap = new HashMap<>();
        predefinedRowsTypeMap.put("file.field1.type", "String");
        predefinedRowsTypeMap.put("file.field2.type", "String");
        predefinedRowsTypeMap.put("file.field3.type", "String");
        predefinedRowsTypeMap.put("file.field4.type", "Date");
        predefinedRowsTypeMap.put("file.field5.type", "String");
        reader.checkVersion();
        try {
            reader.read(predefinedRowsTypeMap);
            for (HashMap<String, String> concretePersonMap : reader.list) {
                for (Map.Entry<String, String> entry : concretePersonMap.entrySet()) {
                    System.out.print(entry.getValue() + " ");
                }
                System.out.print("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
