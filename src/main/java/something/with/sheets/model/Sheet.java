package something.with.sheets.model;

import something.with.sheets.dto.CreateSheetRequest;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Sheet {
    private final String id;
    private final Map<String, Column> columns;

    public Sheet(String id, List<Column> columnsList) {
        this.id = id;
        this.columns = new HashMap<>();
        for (Column col : columnsList) {
            this.columns.put(col.getName(), col);
        }
    }

    public String getId() {
        return id;
    }

    public Map<String, Column> getColumns() {
        return columns;
    }

    public void setCellValue(int rowIndex, String columnName, Object value) {
        Column column = columns.get(columnName);
        if (column == null) throw new IllegalArgumentException("Column not found");
        column.setCell(rowIndex, value);
    }

    public Object getCellValue(int rowIndex, String columnName) {
        Column column = columns.get(columnName);
        if (column == null) return null;
        return column.getCell(rowIndex);
    }

    public Object getResolvedCellValue(int rowIndex, String columnName) {
        Cell cell = getCell(rowIndex, columnName);
        if (cell == null) return null;
        return cell.getValue();
    }

    public Cell getCell(int rowIndex, String columnName) {
        Column column = columns.get(columnName);
        if (column == null) return null;
        return column.getOrCreateCell(rowIndex);
    }

    public static Sheet fromRequest(String id, CreateSheetRequest request) {
        List<Column> columns = request.getColumns().stream()
            .map(Column::fromDto)
            .collect(Collectors.toList());
        return new Sheet(id, columns);
    }
}
