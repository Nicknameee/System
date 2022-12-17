package spring.application.tree.data.users.attributes;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum Role {
    ROLE_ADMIN(Set.of(Permission.ADMIN_PERMISSION)),
    ROLE_SALESMAN(Set.of(Permission.SALESMAN_PERMISSION)),
    ROLE_CUSTOMER(Set.of(Permission.CUSTOMER_PERMISSION));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<SimpleGrantedAuthority> getAuthorities() {
        return permissions.stream().
                map(permission -> new SimpleGrantedAuthority(permission.getPermission())).
                collect(Collectors.toSet());
    }
}