package something.with.sheets.model;

import something.with.sheets.dto.CreateSheetRequest;
import java.util.List;
import java.util.stream.Collectors;

public class Sheet {
    private final String id;
    private final List<Column> columns;

    public Sheet(String id, List<Column> columns) {
        this.id = id;
        this.columns = columns;
    }

    public String getId() {
        return id;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public static Sheet fromRequest(String id, CreateSheetRequest request) {
        List<Column> columns = request.getColumns().stream()
            .map(Column::fromDto)
            .collect(Collectors.toList());
        return new Sheet(id, columns);
    }
}
