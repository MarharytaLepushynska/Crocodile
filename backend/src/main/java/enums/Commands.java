package enums;

public enum Commands {
    GET_PRODUCT_QUANTITY(0),
    DEL_PRODUCT_AMOUNT(1),
    ADD_PRODUCT_AMOUNT(2),
    ADD_GROUP(3),
    ADD_PRODUCT_TO_GROUP(4),
    SET_PRICE(5),
    CREATE_PRODUCT(6),
    READ_PRODUCT(7),
    UPDATE_PRODUCT(8),
    DELETE_PRODUCT(9),
    SEARCH_PRODUCTS(10),
    CALLBACK(11);

    public final int cType;

    Commands(int i) {
        cType = i;
    }

    public static Commands fromCType(int cType) {
        for (Commands command : values()) {
            if (command.cType == cType) {
                return command;
            }
        }
        return CALLBACK;
    }
}
