import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import report_config.ReportConfig;
import test_dtos.Samaple2Sub;
import test_dtos.SampleDto;
import test_dtos.SampleSubClassDto;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.*;

public class SpreadSheetGenerator {
    public static void main(String... args) {

        ReportConfig reportConfig = new ReportConfig();
        reportConfig.reportFieldSelector.add(new ReportConfig.FieldSelector("name", "Name", "test_dtos.SampleDto.name", true));
        reportConfig.reportFieldSelector.add(new ReportConfig.FieldSelector("numericId", "Numeric Integer", "test_dtos.SampleDto.numericId", true));
        reportConfig.reportFieldSelector.add(new ReportConfig.FieldSelector("textList", "Text List", "test_dtos.SampleDto.textList", true));
        reportConfig.reportFieldSelector.add(new ReportConfig.FieldSelector("abc", "ABC", "test_dtos.Samaple2Sub.abc", true));
        reportConfig.reportFieldSelector.add(new ReportConfig.FieldSelector("txt", "Subtxt", "test_dtos.SampleSubClassDto.txt", true));


        try {
            Set<SampleSubClassDto.Test> l2 = new ListOrderedSet<SampleSubClassDto.Test>();
//            l2.add("AAA");
//            l2.add("BBB");
//            l2.add("CCC");
            ((ListOrderedSet<SampleSubClassDto.Test>) l2).add(new SampleSubClassDto.Test(555, "NestedListObj1"));
            ((ListOrderedSet<SampleSubClassDto.Test>) l2).add(new SampleSubClassDto.Test(666,"NestedListObj1"));
            ((ListOrderedSet<SampleSubClassDto.Test>) l2).add(new SampleSubClassDto.Test(777,"NestedListObj1"));
            List<SampleDto> dtos = Arrays.asList(
                    new SampleDto("Name1", 1, Arrays.asList("AA", "BB", "CC"), new SampleSubClassDto(11, "Sub AA", l2, new Samaple2Sub(999, "ZZZ")))
//                    ,new SampleDto("Name2", 2, Arrays.asList("XX", "YY", "ZZ"), new SampleSubClassDto(22, "Sub BB", l2, new Samaple2Sub(999, "ZZZ")))
            );

            generateSpreadSheet("Test.xlsx", dtos, reportConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static <T> void generateSpreadSheet(String filename, List<T> dtos, ReportConfig reportConfig) throws Exception {
        Workbook workbook = null;
        if (filename.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook();
        } else if (filename.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook();
        } else {
            throw new Exception("Error, File name should be .xls or .xlsx");
        }

        Sheet sheet = workbook.createSheet();

        CellCoordinate cellCoordinate = new CellCoordinate(0, 0);
        Iterator<T> iterator = dtos.iterator();

        while (iterator.hasNext()) {
            T dto = iterator.next();
            System.out.println("Writing the row :" + dto);
            cellCoordinate.colId = 0;
            populateRowCell(sheet.createRow(cellCoordinate.rowId), cellCoordinate, 0, 0, dto, reportConfig);
        }

        FileOutputStream fileOutputStream = new FileOutputStream(filename);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        System.out.println(filename + " written successfully");
    }

    public static <T> CellCoordinate populateRowCell(Row row, CellCoordinate currentCellCoordinate, int maxRowIndex, int maxColIndex, T dto, ReportConfig reportConfig) throws IllegalAccessException {
        Field[] fields = dto.getClass().getDeclaredFields();
        for (Field field : fields) {

            field.setAccessible(true);

            if (field.getType().isPrimitive() ||
                    Arrays.asList(Long.TYPE, Integer.TYPE, Byte.TYPE, Character.TYPE, Double.TYPE, Boolean.TYPE, Byte.TYPE,
                            Long.class, Integer.class, Byte.class, Character.class, Double.class, Boolean.class, Byte.class).contains(field.getType()) ||
                    field.getType().equals(String.class)
            ) {

                if (!reportConfig.isFieldToShow(dto.getClass().getCanonicalName().concat(".").concat(field.getName()))) {
                    continue;
                }


                System.out.println("Writing the cell simple value :" + field.get(dto).toString());

                writeToCell(row.createCell(currentCellCoordinate.colId++), field.get(dto).toString());

            } else if (Collection.class.isAssignableFrom(field.getType())) {
                if (!reportConfig.isFieldToShow(dto.getClass().getCanonicalName().concat(".").concat(field.getName()))) {
                    continue;
                }

                System.out.println("Writing the cell List value :" + field.get(dto).toString());
                Iterator iterator = ((Collection) field.get(dto)).iterator();
                Row subRow;
                CellCoordinate nestedCellCoordinate = new CellCoordinate(currentCellCoordinate.rowId, currentCellCoordinate.colId++);
                do {
                    subRow = row.getSheet().getRow(nestedCellCoordinate.rowId);
                    if (subRow == null) {
                        subRow = row.getSheet().createRow(nestedCellCoordinate.rowId);
                    }
                    System.out.println("Writing a new sub-row");

                    Object collectionItem = iterator.next();
                    if (collectionItem instanceof String) {
                        writeToCell(subRow.createCell(nestedCellCoordinate.colId), collectionItem.toString());
                    } else {
                        nestedCellCoordinate = populateRowCell(subRow, nestedCellCoordinate, maxRowIndex, nestedCellCoordinate.colId, collectionItem, reportConfig);
                    }
                    nestedCellCoordinate.rowId++;

                } while (iterator.hasNext());

                if (maxRowIndex < nestedCellCoordinate.rowId) {
                    maxRowIndex = nestedCellCoordinate.rowId;
                }
                if (maxColIndex < nestedCellCoordinate.colId) {
                    maxColIndex = nestedCellCoordinate.colId;
                }
            } else {
                System.out.println("Writing the cell other/object value :" + field.get(dto).toString());
                return populateRowCell(row, currentCellCoordinate, maxRowIndex, maxColIndex, field.get(dto), reportConfig);
            }


        }

        currentCellCoordinate.rowId = maxRowIndex;
        currentCellCoordinate.colId = maxColIndex;
        return currentCellCoordinate;
    }

    public static void writeToCell(Cell cell, String value) {
        System.out.println("Writing to cell: " + cell.getRowIndex() + " : " + cell.getColumnIndex() + " : " + value);
        cell.setCellValue(value);
    }

    public static class CellCoordinate {
        public int rowId;
        public int colId;

        public CellCoordinate(int rowId, int colId) {
            this.rowId = rowId;
            this.colId = colId;
        }

        public CellCoordinate(CellCoordinate cellCoordinate) {
            this.rowId = cellCoordinate.rowId;
            this.colId = cellCoordinate.colId;
        }
    }

}
