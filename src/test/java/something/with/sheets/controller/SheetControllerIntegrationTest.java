package something.with.sheets.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import something.with.sheets.dto.ColumnDto;
import something.with.sheets.dto.CreateSheetRequest;
import something.with.sheets.dto.CreateSheetResponse;
import something.with.sheets.dto.GetSheetResponse;
import something.with.sheets.dto.SetCellValueRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SheetControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/sheets";
    }

    @Test
    void createSheet_and_getSheetById() {
        // Create a sheet
        var request = new CreateSheetRequest();
        var colA = new ColumnDto();
        colA.setName("A");
        colA.setType("boolean");
        var colB = new ColumnDto();
        colB.setName("B");
        colB.setType("int");
        request.setColumns(java.util.Arrays.asList(colA, colB));

        ResponseEntity<CreateSheetResponse> createResp = restTemplate.postForEntity(baseUrl, request, CreateSheetResponse.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String sheetId = createResp.getBody().getId();
        assertThat(sheetId).isNotBlank();

        // Set a cell value
        SetCellValueRequest cellReq = new SetCellValueRequest();
        cellReq.setRowIndex(0);
        cellReq.setColumnName("A");
        cellReq.setValue(true);
        ResponseEntity<Void> setCellResp = restTemplate.postForEntity(baseUrl + "/" + sheetId + "/cell", cellReq, Void.class);
        assertThat(setCellResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Get the sheet
        ResponseEntity<GetSheetResponse> getResp = restTemplate.getForEntity(baseUrl + "/" + sheetId, GetSheetResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        GetSheetResponse sheet = getResp.getBody();
        assertThat(sheet.getId()).isEqualTo(sheetId);
        assertThat(sheet.getColumns()).hasSize(2);
        assertThat(sheet.getColumns().get(0).getName()).isEqualTo("A");
        assertThat(sheet.getColumns().get(0).getValues().get(0)).isEqualTo(true);
    }

    @Test
    void setCellValue_withLookup_and_getSheetById() {
        // Create a sheet with two int columns
        var request = new CreateSheetRequest();
        var colA = new ColumnDto();
        colA.setName("A");
        colA.setType("int");
        var colB = new ColumnDto();
        colB.setName("B");
        colB.setType("int");
        request.setColumns(java.util.Arrays.asList(colA, colB));

        ResponseEntity<CreateSheetResponse> createResp = restTemplate.postForEntity(baseUrl, request, CreateSheetResponse.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String sheetId = createResp.getBody().getId();
        assertThat(sheetId).isNotBlank();

        // Set A[0] = 42
        SetCellValueRequest setA = new SetCellValueRequest();
        setA.setRowIndex(0);
        setA.setColumnName("A");
        setA.setValue(42);
        ResponseEntity<Void> setAResp = restTemplate.postForEntity(baseUrl + "/" + sheetId + "/cell", setA, Void.class);
        assertThat(setAResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Set B[0] = lookup(A,0)
        SetCellValueRequest setB = new SetCellValueRequest();
        setB.setRowIndex(0);
        setB.setColumnName("B");
        setB.setValue("lookup(A,0)");
        ResponseEntity<Void> setBResp = restTemplate.postForEntity(baseUrl + "/" + sheetId + "/cell", setB, Void.class);
        assertThat(setBResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Get the sheet and verify B[0] resolves to 42
        ResponseEntity<GetSheetResponse> getResp = restTemplate.getForEntity(baseUrl + "/" + sheetId, GetSheetResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        GetSheetResponse sheet = getResp.getBody();
        assertThat(sheet.getId()).isEqualTo(sheetId);
        assertThat(sheet.getColumns()).hasSize(2);
        GetSheetResponse.ColumnData colAData = sheet.getColumns().stream().filter(c -> c.getName().equals("A")).findFirst().orElseThrow();
        GetSheetResponse.ColumnData colBData = sheet.getColumns().stream().filter(c -> c.getName().equals("B")).findFirst().orElseThrow();
        assertThat(colAData.getValues().get(0)).isEqualTo(42);
        assertThat(colBData.getValues().get(0)).isEqualTo(42);
    }
} 