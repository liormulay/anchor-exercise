package something.with.sheets.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import something.with.sheets.dto.ColumnDto;
import something.with.sheets.dto.CreateSheetRequest;
import something.with.sheets.model.Sheet;
import something.with.sheets.repository.SheetRepository;

import java.util.Arrays;
import java.util.List;

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
        List<?> columns = savedSheet.getColumns();
        assertEquals(2, columns.size());
        assertEquals("A", savedSheet.getColumns().get(0).getName());
        assertEquals("boolean", savedSheet.getColumns().get(0).getType());
    }
} 