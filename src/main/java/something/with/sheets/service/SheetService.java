package something.with.sheets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import something.with.sheets.dto.CreateSheetRequest;
import something.with.sheets.dto.SetCellValueRequest;
import something.with.sheets.model.Column;
import something.with.sheets.model.Sheet;
import something.with.sheets.repository.SheetRepository;
import java.util.UUID;
import something.with.sheets.model.Cell;
import something.with.sheets.dto.GetSheetResponse;
import java.util.List;
import java.util.stream.Collectors;

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
        if (isLookupValue(value)) {
            handleLookupValue(sheet, column, request, value, type);
        } else {
            handleNormalValue(column, request, value, type);
        }
        sheetRepository.save(sheet);
    }

    private boolean isLookupValue(Object value) {
        return value instanceof String && ((String) value).startsWith("lookup(");
    }

    private void handleLookupValue(Sheet sheet, Column column, SetCellValueRequest request, Object value, String type) {
        LookupReference ref = parseLookupReference((String) value);
        Column refColumn = sheet.getColumns().get(ref.refCol);
        if (refColumn == null) throw new IllegalArgumentException("Referenced column not found");
        Cell refCell = refColumn.getOrCreateCell(ref.refRow);
        if (!type.equals(refColumn.getType())) {
            throw new IllegalArgumentException("Type mismatch for lookup: " + type + " vs " + refColumn.getType());
        }
        Cell targetCell = column.getOrCreateCell(request.getRowIndex());
        if (refCell == targetCell || refCell.hasCycle(targetCell)) {
            throw new IllegalArgumentException("Cycle detected in lookup");
        }
        targetCell.setLookup(refCell);
    }

    private void handleNormalValue(Column column, SetCellValueRequest request, Object value, String type) {
        Cell cell = column.getOrCreateCell(request.getRowIndex());
        cell.clearLookup();
        if (!isValueOfType(value, type)) {
            throw new IllegalArgumentException("Value does not match column type: " + type);
        }
        cell.setValue(value);
    }

    private LookupReference parseLookupReference(String lookupStr) {
        // Parse lookup("A",10) or lookup(A,10)
        String inner = lookupStr.substring(7, lookupStr.length() - 1).trim();
        String[] parts = inner.split(",");
        if (parts.length != 2) throw new IllegalArgumentException("Invalid lookup syntax");
        String refCol = parts[0].replaceAll("[\"']", "").trim();
        int refRow;
        try {
            refRow = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid row index in lookup");
        }
        return new LookupReference(refCol, refRow);
    }

    private static class LookupReference {
        String refCol;
        int refRow;
        LookupReference(String refCol, int refRow) {
            this.refCol = refCol;
            this.refRow = refRow;
        }
    }

    public GetSheetResponse getSheetById(String sheetId) {
        Sheet sheet = sheetRepository.findById(sheetId);
        if (sheet == null) {
            throw new IllegalArgumentException("Sheet not found");
        }
        List<GetSheetResponse.ColumnData> columns = sheet.getColumns().values().stream()
            .map(col -> new GetSheetResponse.ColumnData(
                col.getName(),
                col.getType(),
                col.getCells().stream().map(cell -> cell != null ? cell.getValue() : null).collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
        return new GetSheetResponse(sheet.getId(), columns);
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