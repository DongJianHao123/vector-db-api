package demo.api.vectorDB.entity;

public enum QueryType {
    DOCUMENT_NAME(1), TITLE(2);

    private Integer val;

    private QueryType(Integer val) {
        this.val = val;
    }

    public Integer getVal() {
        return val;
    }

    public void setVal(Integer val) {
        this.val = val;
    }
}
