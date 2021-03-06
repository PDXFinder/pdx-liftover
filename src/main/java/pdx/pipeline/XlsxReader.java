package pdx.pipeline;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class XlsxReader {

    Logger log = LoggerFactory.getLogger(XlsxReader.class);

    Optional<Workbook> xslxWorkbook;
    ArrayList<ArrayList<String>> sheetData = null;

    public ArrayList<ArrayList<String>> readFirstSheet(File xlsx) {

        xslxWorkbook = getWorkbook(xlsx);

        if( xslxWorkbook.isPresent() ){
            sheetData = iterateThroughSheet(getSheet(xslxWorkbook));
        }
        return sheetData;
    }

    private Optional<Workbook> getWorkbook(File file) {

        if (!file.exists()) return Optional.empty();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            return Optional.of(workbook);

        } catch (IOException e) {
            log.error("There was a problem accessing the file: {}", e);
        }
        return Optional.empty();
    }

    public ArrayList<ArrayList<String>> iterateThroughSheet(Sheet sheet) {

        ArrayList<ArrayList<String>> listOfCellLists = new ArrayList<>();

        for (Row currentRow : sheet) {

            ArrayList<String> cellValues = getCellValues(currentRow);

            listOfCellLists.add(cellValues);
        }
        return listOfCellLists;
    }

    private ArrayList<String> getCellValues(Row currentRow){

        ArrayList<String> cellValues = new ArrayList<>();

        for(int i = 0; i < currentRow.getLastCellNum(); i++) {

            Cell cell = (currentRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
            if(cellIsNotNullOrEmpty(cell))
                cellValues.add("");
            else
                cellValues.add(getString(cell));
        }
        return cellValues;
    }

    private boolean cellIsNotNullOrEmpty(Cell cell){
        return (cell.getCellType() == Cell.CELL_TYPE_BLANK);
    }

    private String getCellValueAsString(Cell currentCell) {
        return getString(currentCell);
    }

    public static String getString(Cell currentCell) {

        String value;

        switch (currentCell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                value = cleanSpaces(
                            currentCell.getStringCellValue());
                break;

            case Cell.CELL_TYPE_NUMERIC:
                value = toStringFormatFactory(currentCell.getNumericCellValue());
                break;

            default:
                value = "";
                break;
        }
        return value;
    }
    private static String toStringFormatFactory(double cellNum){

        String matchSciNotation = "^\\d\\.\\d{1,11}E\\d{1,2}$";
        String matchTrailingZeros = "^\\d{1,11}\\.0{1,10}$";

        String formattedNum;
        String longToParse = cleanSpaces(String.valueOf(cellNum));
        if (longToParse.matches(matchSciNotation)){
            formattedNum = String.format("%.0f", cellNum);
        }
        else if(longToParse.matches(matchTrailingZeros)) {
            formattedNum = String.valueOf((int) cellNum);
        }
        else {
           formattedNum = longToParse;
        }
        return formattedNum;
    }


    private static String cleanSpaces(String stringToClean) {
        return stringToClean.trim();
    }

    private Sheet getSheet(Optional<Workbook> workbook) {
        return workbook.get().getSheetAt(0);
    }
}
