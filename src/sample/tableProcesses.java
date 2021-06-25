package sample;

public class tableProcesses {
    int tableIndex;
    String tableName;
    int tableMemory;
    int tableDuration;
    int tableStatus;

    public tableProcesses(int tableIndex, String tableName, int tableMemory, int tableDuration, int tableStatus) {
        this.tableIndex = tableIndex;
        this.tableName = tableName;
        this.tableMemory = tableMemory;
        this.tableDuration = tableDuration;
        this.tableStatus = tableStatus;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getTableMemory() {
        return tableMemory;
    }

    public void setTableMemory(int tableMemory) {
        this.tableMemory = tableMemory;
    }

    public int getTableDuration() {
        return tableDuration;
    }

    public void setTableDuration(int tableDuration) {
        this.tableDuration = tableDuration;
    }

    public int getTableStatus() {
        return tableStatus;
    }

    public void setTableStatus(int tableStatus) {
        this.tableStatus = tableStatus;
    }
}
