package something.with.sheets.dto;

import java.util.List;

public class CreateSheetRequest {
    private List<ColumnDto> columns;

    public List<ColumnDto> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDto> columns) {
        this.columns = columns;
    }
} 