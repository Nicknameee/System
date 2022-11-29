package spring.application.tree.data.users.attributes;

import lombok.Getter;

@Getter
public enum Permission {
    ADMIN_PERMISSION("admin::permission"),
    SALESMAN_PERMISSION("salesman::permission"),
    ACCOUNTANT_PERMISSION("accountant::permission"),
    CUSTOMER_PERMISSION("customer::permission");

    private final String permission;

    Permission(String permission)
    {
        this.permission = permission;
    }
}