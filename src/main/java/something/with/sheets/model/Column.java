package something.with.sheets.model;

import something.with.sheets.dto.ColumnDto;
import something.with.sheets.model.Cell;

import java.util.ArrayList;
import java.util.List;

public class Column {
    private final String name;
    private final String type;
    private final List<Cell> cells = new ArrayList<>();

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

    public List<Cell> getCells() {
        return cells;
    }

    public void ensureCellExists(int rowIndex) {
        while (cells.size() <= rowIndex) {
            cells.add(new Cell(null));
        }
    }

    public void setCell(int rowIndex, Object value) {
        ensureCellExists(rowIndex);
        cells.get(rowIndex).setValue(value);
    }

    public Object getCell(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= cells.size()) {
            return null;
        }
        return cells.get(rowIndex).getValue();
    }

    public Cell getOrCreateCell(int rowIndex) {
        ensureCellExists(rowIndex);
        return cells.get(rowIndex);
    }

    public void setCellLookup(int rowIndex, Cell target) {
        ensureCellExists(rowIndex);
        cells.get(rowIndex).setLookup(target);
    }

    public static Column fromDto(ColumnDto dto) {
        return new Column(dto.getName(), dto.getType());
    }
}