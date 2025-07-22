package something.with.sheets.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import something.with.sheets.service.SheetService;
import something.with.sheets.dto.CreateSheetRequest;
import something.with.sheets.dto.CreateSheetResponse;
import something.with.sheets.dto.SetCellValueRequest;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/sheets")
public class SheetController {
    private final SheetService sheetService;

    @Autowired
    public SheetController(SheetService sheetService) {
        this.sheetService = sheetService;
    }

    @PostMapping
    public ResponseEntity<CreateSheetResponse> createSheet(@RequestBody CreateSheetRequest request) {
        String sheetId = sheetService.createSheet(request);
        return ResponseEntity.ok(new CreateSheetResponse(sheetId));
    }

    @PostMapping("/{sheetId}/cell")
    public ResponseEntity<?> setCellValue(@PathVariable String sheetId, @RequestBody SetCellValueRequest request) {
        try {
            sheetService.setCellValue(sheetId, request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
            }
            return ResponseEntity.badRequest().body(msg);
        }
    }
} 