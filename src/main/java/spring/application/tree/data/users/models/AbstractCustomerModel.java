package spring.application.tree.data.users.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class AbstractCustomerModel extends AbstractUserModel {
    private Integer id;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String address;

    public void updatePersonalData(AbstractCustomerModel abstractCustomerModel) {
        if (abstractCustomerModel.getFirstName() != null) {
            this.firstName = abstractCustomerModel.getFirstName();
        }
        if (abstractCustomerModel.getLastName() != null) {
            this.lastName = abstractCustomerModel.getLastName();
        }
        if (abstractCustomerModel.getAddress() != null) {
            this.address = abstractCustomerModel.getAddress();
        }
    }

    public void mergeChanges(AbstractUserModel abstractUserModel) {
        if (!Objects.equals(this.id, abstractUserModel.getId())) {
            return;
        }
        if (abstractUserModel.getPassword() != null) {
            this.setPassword(abstractUserModel.getPassword());
        }
        if (abstractUserModel.getTimezone() != null) {
            this.setTimezone(abstractUserModel.getTimezone());
        }
    }
}
