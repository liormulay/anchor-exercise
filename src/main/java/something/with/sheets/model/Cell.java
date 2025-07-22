package something.with.sheets.model;

public class Cell {
    private Object value;
    Cell lookUpCell;

    public Cell(Object value) {
        this.value = value;
    }

    public boolean isLookup() {
        return lookUpCell != null;
    }

    public void setLookup(Cell target) {
        this.lookUpCell = target;
    }

    public void clearLookup() {
        this.lookUpCell = null;
    }

    public Object getValue() {
        if (lookUpCell != null) {
            return lookUpCell.getValue();
        }
        return value;
    }

    public boolean hasCycle(Cell target) {
        Cell current = this;
        while (current != null) {
            if (current == target) return true;
            current = current.lookUpCell;
        }
        return false;
    }

    public void setValue(Object value) {
        this.value = value;
    }
} 