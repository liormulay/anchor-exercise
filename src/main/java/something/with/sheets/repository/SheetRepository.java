package something.with.sheets.repository;

import org.springframework.stereotype.Repository;
import something.with.sheets.model.Sheet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SheetRepository {
    private final Map<String, Sheet> sheets = new ConcurrentHashMap<>();

    public void save(Sheet sheet) {
        sheets.put(sheet.getId(), sheet);
    }

    public Sheet findById(String id) {
        return sheets.get(id);
    }
} 