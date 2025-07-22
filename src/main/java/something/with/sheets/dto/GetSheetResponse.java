package something.with.sheets.dto;

import java.util.List;

public class GetSheetResponse {
    private String id;
    private List<ColumnData> columns;

    public GetSheetResponse(String id, List<ColumnData> columns) {
        this.id = id;
        this.columns = columns;
    }

    public String getId() {
        return id;
    }

    public List<ColumnData> getColumns() {
        return columns;
    }

    public static class ColumnData {
        private String name;
        private String type;
        private List<Object> values;

        public ColumnData(String name, String type, List<Object> values) {
            this.name = name;
            this.type = type;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public List<Object> getValues() {
            return values;
        }
    }
} 