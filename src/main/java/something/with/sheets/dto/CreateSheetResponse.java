package something.with.sheets.dto;

public class CreateSheetResponse {
    private String id;

    public CreateSheetResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
} 