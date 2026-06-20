package enums;

public enum DrawColor {
    BLACK("#000000"),
    RED("#FF0000"),
    GREEN("#00A300"),
    BLUE("#0066FF"),
    YELLOW("#FFD500"),
    WHITE("#FFFFFF"); // стирачка ? 

    public final String hex;

    DrawColor(String hex) {
        this.hex = hex;
    }

    public static DrawColor fromIndex(int index) {
        return values()[index];
    }
}