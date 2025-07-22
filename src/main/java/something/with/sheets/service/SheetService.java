package something.with.sheets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import something.with.sheets.dto.CreateSheetRequest;
import something.with.sheets.dto.SetCellValueRequest;
import something.with.sheets.model.Column;
import something.with.sheets.model.Sheet;
import something.with.sheets.repository.SheetRepository;
import java.util.UUID;

@Service
public class SheetService {
    private final SheetRepository sheetRepository;

    @Autowired
    public SheetService(SheetRepository sheetRepository) {
        this.sheetRepository = sheetRepository;
    }

    public String createSheet(CreateSheetRequest request) {
        String id = UUID.randomUUID().toString();
        Sheet sheet = Sheet.fromRequest(id, request);
        sheetRepository.save(sheet);
        return id;
    }

    public void setCellValue(String sheetId, SetCellValueRequest request) {
        Sheet sheet = sheetRepository.findById(sheetId);
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found");
        }
        Column column = sheet.getColumns().get(request.getColumnName());
        if (column == null) {
            throw new IllegalArgumentException("Column not found");
        }
        Object value = request.getValue();
        String type = column.getType();
        if (!isValueOfType(value, type)) {
            throw new IllegalArgumentException("Value does not match column type: " + type);
        }
        sheet.setCellValue(request.getRowIndex(), request.getColumnName(), value);
        sheetRepository.save(sheet);
    }

    private boolean isValueOfType(Object value, String type) {
        if (value == null) return false;
        switch (type) {
            case "int":
                return value instanceof Integer || (value instanceof Number && ((Number) value).intValue() == ((Number) value).doubleValue());
            case "boolean":
                return value instanceof Boolean;
            case "string":
                return value instanceof String;
            case "double":
                return value instanceof Double || value instanceof Float || value instanceof Integer || value instanceof Long;
            default:
                return false;
        }
    }
} 