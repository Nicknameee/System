package spring.application.tree.data.users.attributes;

import java.util.Arrays;

public enum Status {
    ENABLED, DISABLED;

    public static Status fromOrdinal(int status) {
        return Arrays.stream(Status.values()).filter(s -> s.ordinal() == status).findFirst().orElse(null);
    }
}