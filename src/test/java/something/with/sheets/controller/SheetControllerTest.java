package something.with.sheets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import something.with.sheets.config.SecurityConfig;
import something.with.sheets.dto.ColumnDto;
import something.with.sheets.dto.CreateSheetRequest;
import something.with.sheets.dto.CreateSheetResponse;
import something.with.sheets.service.SheetService;

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
} 