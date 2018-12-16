package mailsender;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by a.shipulin on 06.09.18.
 */
public class AddressBookReader {
    public String path;
    public ArrayList<HashMap<String, String>> addressBook;
    public ArrayList<String> errors; // массив ошибок при чтении файла адресной книги


    public AddressBookReader(String path, ArrayList<HashMap<String, String>> list) {
        this.path = path;
        this.errors = new ArrayList();
    }

    public void read(HashMap<String, String> rowsTypeMap) throws IOException, InvalidFormatException, ParseException {
        String version = checkVersion();
        if (version.equals("OOXML")) {
            readXLSX(rowsTypeMap);
        } else {
            throw new IOException("Адресная книга имеет неподдерживаемый формат.");
        }
    }

    public void readXLSXRows(Iterator<Row> rowIterator, HashMap<String, String> rowsTypeMap) throws ParseException {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        HashMap<String, String> person;
        long rowNumber = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            rowNumber++;
            Iterator<Cell> cellIterator = row.cellIterator();
            person = new HashMap<String, String>();
            Date birthdayDate = new Date();
            String cur_value;
            int j = 0;
            boolean rowIsError = false;
            while (cellIterator.hasNext()) {
                j++;
                String fieldNumStr = "field" + j;
                Cell cell = cellIterator.next();

                String fieldNumType = rowsTypeMap.get("excel." + fieldNumStr + ".type");
                CellType currCellType = cell.getCellTypeEnum();
                if (checkCellType(currCellType, fieldNumType)) {
                    if (fieldNumType.equalsIgnoreCase("date")) {
                        DataFormatter dataFormatter = new DataFormatter();
                        dataFormatter.formatCellValue(cell);
                        Double dateValue = cell.getNumericCellValue();
                        Date javaDate = DateUtil.getJavaDate(dateValue);
                        String dateStr = new SimpleDateFormat("dd.MM.yyyy").format(javaDate);
                        person.put(fieldNumStr, dateStr);
                    } else if (fieldNumType.equalsIgnoreCase("string")) {
                        String valueString = (String) cell.getStringCellValue();
                        person.put(fieldNumStr, valueString);
                    } else if (fieldNumType.equalsIgnoreCase("number")) {
                        Double valueDouble = (Double) cell.getNumericCellValue();
                        person.put(fieldNumStr, valueDouble.toString());
                    }
                } else {
                    this.errors.add("Ошибка в адресной книге: Формат ячеек в строке " + rowNumber + " не соответствует настройкам files.properties");
                    rowIsError = true;
                    break;
                }
            }
            if(rowIsError == false) {
                list.add(person);
            }
        }
        this.addressBook = list;
    }

    private boolean checkCellType(CellType currCellType, String fieldNumType) {
        if (currCellType == CellType.STRING && fieldNumType.equalsIgnoreCase("string")) {
            return true;
        } else if ((currCellType == CellType.NUMERIC) && (fieldNumType.equalsIgnoreCase("number") || fieldNumType.equalsIgnoreCase("date"))) {
            return true;
        } else return false;
    }


    public void readXLSX(HashMap<String, String> rowsTypeMap) throws IOException, InvalidFormatException, ParseException {
        InputStream inputStream = new FileInputStream(this.path);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
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
        // диагностика чтения файла адресной книги
        //use under command line:  java -cp MailSender.jar mailsender.AddressBookReader path_to_adressbook_file

        ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

        AddressBookReader reader = null;
        try {
            //reader = new AddressBookReader("D:\\Строев\\Расчетные листки\\Адресная книга - копия (2).xlsx", arrayList);
            reader = new AddressBookReader(args[0], arrayList);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }



        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("files.properties"));
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла files.properties: " + e.getMessage());
        }

        HashMap<String, String> predefinedRowsTypeMap = initRowTypeMap(properties);
        TreeMap<String, String> sortedMap = new TreeMap<String, String>(predefinedRowsTypeMap);
        reader.checkVersion();
        try {
            reader.read(predefinedRowsTypeMap);
            for (HashMap<String, String> concretePersonMap : reader.addressBook) {
                for (Map.Entry<String, String> entry : concretePersonMap.entrySet()) {
                    System.out.print(entry.getValue() + " ");
                }
                System.out.print("\n");
            }
            if(reader.errors.size()!=0) {
                for (int i = 0; i < reader.errors.size(); i++) {
                    System.out.println(reader.errors.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> initRowTypeMap(Properties properties) {
        Map<String, String> map = properties.entrySet().stream().collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
        Map<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile("excel.field[0-9]+.type");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Matcher matcher = pattern.matcher(entry.getKey());
            if (matcher.matches()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return (HashMap<String, String>) result;
    }
}
