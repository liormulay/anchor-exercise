package something.with.sheets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import something.with.sheets.dto.ColumnDto;
import something.with.sheets.dto.CreateSheetRequest;
import something.with.sheets.service.SheetService;
import something.with.sheets.dto.SetCellValueRequest;
import something.with.sheets.dto.GetSheetResponse;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SheetControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SheetService sheetService;

    @Test
    void createSheet_ReturnsSheetId() throws Exception {
        CreateSheetRequest request = new CreateSheetRequest();
        ColumnDto colA = new ColumnDto();
        colA.setName("A");
        colA.setType("boolean");
        ColumnDto colB = new ColumnDto();
        colB.setName("B");
        colB.setType("int");
        request.setColumns(Arrays.asList(colA, colB));

        String expectedId = "sheet-123";
        when(sheetService.createSheet(any(CreateSheetRequest.class))).thenReturn(expectedId);

        mockMvc.perform(post("/sheets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedId));
    }

    @Test
    void setCellValue_Success() throws Exception {
        SetCellValueRequest req = new SetCellValueRequest();
        req.setRowIndex(0);
        req.setColumnName("A");
        req.setValue(true);
        // No exception means success
        org.mockito.Mockito.doNothing().when(sheetService).setCellValue(org.mockito.Mockito.anyString(), org.mockito.Mockito.any(SetCellValueRequest.class));
        mockMvc.perform(post("/sheets/sheet-1/cell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void setCellValue_TypeError() throws Exception {
        SetCellValueRequest req = new SetCellValueRequest();
        req.setRowIndex(0);
        req.setColumnName("A");
        req.setValue(123); // Not a boolean
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Value does not match column type: boolean"))
                .when(sheetService).setCellValue(org.mockito.Mockito.anyString(), org.mockito.Mockito.any(SetCellValueRequest.class));
        mockMvc.perform(post("/sheets/sheet-1/cell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Value does not match column type")));
    }

    @Test
    void setCellValue_NotFound() throws Exception {
        SetCellValueRequest req = new SetCellValueRequest();
        req.setRowIndex(0);
        req.setColumnName("A");
        req.setValue(true);
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Sheet not found"))
                .when(sheetService).setCellValue(org.mockito.Mockito.anyString(), org.mockito.Mockito.any(SetCellValueRequest.class));
        mockMvc.perform(post("/sheets/sheet-1/cell")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sheet not found")));
    }

    @Test
    void getSheetById_Success() throws Exception {
        String sheetId = "sheet-123";
        GetSheetResponse.ColumnData colA = new GetSheetResponse.ColumnData("A", "boolean", Arrays.asList(true, false));
        GetSheetResponse.ColumnData colB = new GetSheetResponse.ColumnData("B", "int", Arrays.asList(1, 2));
        GetSheetResponse response = new GetSheetResponse(sheetId, Arrays.asList(colA, colB));
        when(sheetService.getSheetById(sheetId)).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/sheets/" + sheetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sheetId))
                .andExpect(jsonPath("$.columns[0].name").value("A"))
                .andExpect(jsonPath("$.columns[0].type").value("boolean"))
                .andExpect(jsonPath("$.columns[0].values[0]").value(true))
                .andExpect(jsonPath("$.columns[1].name").value("B"))
                .andExpect(jsonPath("$.columns[1].type").value("int"))
                .andExpect(jsonPath("$.columns[1].values[1]").value(2));
    }

    @Test
    void getSheetById_NotFound() throws Exception {
        String sheetId = "sheet-404";
        when(sheetService.getSheetById(sheetId)).thenThrow(new IllegalArgumentException("Sheet not found"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/sheets/" + sheetId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sheet not found")));
    }

    @Test
    void getSheetById_BadRequest() throws Exception {
        String sheetId = "sheet-bad";
        when(sheetService.getSheetById(sheetId)).thenThrow(new IllegalArgumentException("Some other error"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/sheets/" + sheetId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Some other error")));
    }
} 