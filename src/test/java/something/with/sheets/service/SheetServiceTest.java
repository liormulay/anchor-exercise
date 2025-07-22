package something.with.sheets.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import something.with.sheets.dto.ColumnDto;
import something.with.sheets.dto.CreateSheetRequest;
import something.with.sheets.dto.SetCellValueRequest;
import something.with.sheets.model.Column;
import something.with.sheets.model.Sheet;
import something.with.sheets.repository.SheetRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SheetServiceTest {
    @Mock
    private SheetRepository sheetRepository;

    @InjectMocks
    private SheetService sheetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createSheet_SavesSheetAndReturnsId() {
        CreateSheetRequest request = new CreateSheetRequest();
        ColumnDto colA = new ColumnDto();
        colA.setName("A");
        colA.setType("boolean");
        ColumnDto colB = new ColumnDto();
        colB.setName("B");
        colB.setType("int");
        request.setColumns(Arrays.asList(colA, colB));

        ArgumentCaptor<Sheet> captor = ArgumentCaptor.forClass(Sheet.class);

        String id = sheetService.createSheet(request);
        assertNotNull(id);
        verify(sheetRepository).save(captor.capture());
        Sheet savedSheet = captor.getValue();
        assertEquals(id, savedSheet.getId());
        Map<String, Column> columns = savedSheet.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.containsKey("A"));
        assertTrue(columns.containsKey("B"));
        assertEquals("boolean", columns.get("A").getType());
    }

    @Test
    void setCellValue_SetsValueAndValidatesType() {
        // Setup
        Column colA = new Column("A", "boolean");
        Column colB = new Column("B", "int");
        Sheet sheet = new Sheet("sheet-1", Arrays.asList(colA, colB));
        when(sheetRepository.findById("sheet-1")).thenReturn(sheet);

        SetCellValueRequest req = new SetCellValueRequest();
        req.setRowIndex(0);
        req.setColumnName("A");
        req.setValue(true);
        sheetService.setCellValue("sheet-1", req);
        assertEquals(true, sheet.getColumns().get("A").getCell(0));

        req.setColumnName("B");
        req.setValue(42);
        sheetService.setCellValue("sheet-1", req);
        assertEquals(42, sheet.getColumns().get("B").getCell(0));
    }

    @Test
    void setCellValue_ThrowsOnTypeMismatch() {
        Column colA = new Column("A", "boolean");
        Sheet sheet = new Sheet("sheet-1", Arrays.asList(colA));
        when(sheetRepository.findById("sheet-1")).thenReturn(sheet);
        SetCellValueRequest req = new SetCellValueRequest();
        req.setRowIndex(0);
        req.setColumnName("A");
        req.setValue(123); // Not a boolean
        Exception ex = assertThrows(IllegalArgumentException.class, () -> sheetService.setCellValue("sheet-1", req));
        assertTrue(ex.getMessage().contains("Value does not match column type"));
    }

    @Test
    void setCellValue_SupportsBasicLookup() {
        // Setup
        Column colA = new Column("A", "int");
        Column colB = new Column("B", "int");
        Sheet sheet = new Sheet("sheet-lookup", Arrays.asList(colA, colB));
        colA.setCell(0, 123);
        when(sheetRepository.findById("sheet-lookup")).thenReturn(sheet);

        // Set B[0] as a lookup to A[0]
        SetCellValueRequest req = new SetCellValueRequest();
        req.setRowIndex(0);
        req.setColumnName("B");
        req.setValue("lookup(A,0)");
        sheetService.setCellValue("sheet-lookup", req);
        // B[0] should resolve to A[0]'s value
        assertEquals(123, sheet.getColumns().get("B").getOrCreateCell(0).getValue());
        // Changing A[0] should reflect in B[0]
        colA.getOrCreateCell(0).setValue(456);
        assertEquals(456, sheet.getColumns().get("B").getOrCreateCell(0).getValue());
    }

    @Test
    void setCellValue_ThrowsOnLookupTypeMismatch() {
        Column colA = new Column("A", "int");
        Column colB = new Column("B", "boolean");
        Sheet sheet = new Sheet("sheet-lookup", Arrays.asList(colA, colB));
        colA.setCell(0, 123);
        when(sheetRepository.findById("sheet-lookup")).thenReturn(sheet);

        SetCellValueRequest req = new SetCellValueRequest();
        req.setRowIndex(0);
        req.setColumnName("B");
        req.setValue("lookup(A,0)");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> sheetService.setCellValue("sheet-lookup", req));
        assertTrue(ex.getMessage().contains("Type mismatch"));
    }

    @Test
    void setCellValue_ThrowsOnLookupToMissingColumnOrRow() {
        Column colA = new Column("A", "int");
        Sheet sheet = new Sheet("sheet-lookup", Arrays.asList(colA));
        when(sheetRepository.findById("sheet-lookup")).thenReturn(sheet);

        SetCellValueRequest req = new SetCellValueRequest();
        req.setRowIndex(0);
        req.setColumnName("A");
        req.setValue("lookup(B,0)"); // B does not exist
        Exception ex1 = assertThrows(IllegalArgumentException.class, () -> sheetService.setCellValue("sheet-lookup", req));
        assertTrue(ex1.getMessage().contains("Referenced column not found"));

        // Now add B, but reference a row that doesn't exist (should create the cell, so no error)
        Column colB = new Column("B", "int");
        sheet.getColumns().put("B", colB);
        req.setValue("lookup(B,5)"); // B[5] does not exist yet
        // Should not throw, should create the cell
        assertDoesNotThrow(() -> sheetService.setCellValue("sheet-lookup", req));
    }

    @Test
    void setCellValue_ThrowsOnLookupCycle() {
        Column colA = new Column("A", "int");
        Sheet sheet = new Sheet("sheet-lookup", Arrays.asList(colA));
        colA.setCell(0, 1);
        colA.setCell(1, 2);
        when(sheetRepository.findById("sheet-lookup")).thenReturn(sheet);

        // A[1] lookup A[0]
        SetCellValueRequest req1 = new SetCellValueRequest();
        req1.setRowIndex(1);
        req1.setColumnName("A");
        req1.setValue("lookup(A,0)");
        sheetService.setCellValue("sheet-lookup", req1);
        // Now try to make A[0] lookup A[1] (cycle)
        SetCellValueRequest req2 = new SetCellValueRequest();
        req2.setRowIndex(0);
        req2.setColumnName("A");
        req2.setValue("lookup(A,1)");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> sheetService.setCellValue("sheet-lookup", req2));
        assertTrue(ex.getMessage().contains("Cycle detected"));
    }

    @Test
    void setCellValue_ClearsLookupWhenOverwritten() {
        Column colA = new Column("A", "int");
        Column colB = new Column("B", "int");
        Sheet sheet = new Sheet("sheet-lookup", Arrays.asList(colA, colB));
        colA.setCell(0, 10);
        colB.setCell(0, 20);
        when(sheetRepository.findById("sheet-lookup")).thenReturn(sheet);

        // Set B[0] as a lookup to A[0]
        SetCellValueRequest req = new SetCellValueRequest();
        req.setRowIndex(0);
        req.setColumnName("B");
        req.setValue("lookup(A,0)");
        sheetService.setCellValue("sheet-lookup", req);
        assertEquals(10, colB.getOrCreateCell(0).getValue());
        // Now overwrite with a normal value
        req.setValue(99);
        sheetService.setCellValue("sheet-lookup", req);
        assertEquals(99, colB.getOrCreateCell(0).getValue());
        // Should no longer be a lookup
        assertFalse(colB.getOrCreateCell(0).isLookup());
    }

    @Test
    void getSheetById_ReturnsCorrectResponse() {
        // Setup
        Column colA = new Column("A", "boolean");
        colA.setCell(0, true);
        colA.setCell(1, false);
        Column colB = new Column("B", "int");
        colB.setCell(0, 42);
        colB.setCell(1, 99);
        Sheet sheet = new Sheet("sheet-123", Arrays.asList(colA, colB));
        when(sheetRepository.findById("sheet-123")).thenReturn(sheet);

        var response = sheetService.getSheetById("sheet-123");
        assertNotNull(response);
        assertEquals("sheet-123", response.getId());
        assertEquals(2, response.getColumns().size());
        var colAData = response.getColumns().stream().filter(c -> c.getName().equals("A")).findFirst().orElse(null);
        assertNotNull(colAData);
        assertEquals("boolean", colAData.getType());
        assertEquals(Arrays.asList(true, false), colAData.getValues());
        var colBData = response.getColumns().stream().filter(c -> c.getName().equals("B")).findFirst().orElse(null);
        assertNotNull(colBData);
        assertEquals("int", colBData.getType());
        assertEquals(Arrays.asList(42, 99), colBData.getValues());
    }

    @Test
    void getSheetById_ThrowsIfSheetNotFound() {
        when(sheetRepository.findById("missing-id")).thenReturn(null);
        Exception ex = assertThrows(IllegalArgumentException.class, () -> sheetService.getSheetById("missing-id"));
        assertTrue(ex.getMessage().contains("Sheet not found"));
    }
} 