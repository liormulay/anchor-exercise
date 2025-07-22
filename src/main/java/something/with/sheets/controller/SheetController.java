package something.with.sheets.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import something.with.sheets.service.SheetService;
import something.with.sheets.dto.CreateSheetRequest;
import something.with.sheets.dto.CreateSheetResponse;

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
} 