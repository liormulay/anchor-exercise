package something.with.sheets.model;

import something.with.sheets.dto.ColumnDto;

import java.util.ArrayList;
import java.util.List;

public class Column {
    private final String name;
    private final String type;
    private final List<Object> cells = new ArrayList<>();

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<Object> getCells() {
        return cells;
    }

    public void ensureCellExists(int rowIndex) {
        while (cells.size() <= rowIndex) {
            cells.add(null);
        }
    }

    public void setCell(int rowIndex, Object value) {
        ensureCellExists(rowIndex);
        cells.set(rowIndex, value);
    }

    public Object getCell(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= cells.size()) {
            return null;
        }
        return cells.get(rowIndex);
    }

    public static Column fromDto(ColumnDto dto) {
        return new Column(dto.getName(), dto.getType());
    }
}