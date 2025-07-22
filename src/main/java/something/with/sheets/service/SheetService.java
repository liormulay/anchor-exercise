package something.with.sheets.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import something.with.sheets.dto.CreateSheetRequest;
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
} 