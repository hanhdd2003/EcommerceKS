package fsa.fresher.ks.ecommerce.model.enums;

public enum Color {
    BLACK("Đen"),
    WHITE("Trắng"),
    RED("Đỏ"),
    BLUE("Xanh"),
    GREEN("Xanh lá");

    private final String displayName;

    Color(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
