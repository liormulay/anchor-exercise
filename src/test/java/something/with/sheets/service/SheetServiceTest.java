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
} 