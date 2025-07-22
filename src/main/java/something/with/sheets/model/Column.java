package something.with.sheets.model;

import something.with.sheets.dto.ColumnDto;

public class Column {
    private final String name;
    private final String type;

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

    public static Column fromDto(ColumnDto dto) {
        return new Column(dto.getName(), dto.getType());
    }
}