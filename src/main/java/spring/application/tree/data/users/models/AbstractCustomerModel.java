package spring.application.tree.data.users.models;

import lombok.Data;

@Data
public class AbstractCustomerModel extends AbstractUserModel {
    private Integer id;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String address;
}
