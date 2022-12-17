package spring.application.tree.data.users.repository.mappers;

import org.springframework.jdbc.core.RowCallbackHandler;
import spring.application.tree.data.users.attributes.Role;
import spring.application.tree.data.users.attributes.Status;
import spring.application.tree.data.users.models.AbstractCustomerModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CustomerMapper implements RowCallbackHandler {
    private final List<AbstractCustomerModel> abstractCustomerModelList;
    public CustomerMapper(List<AbstractCustomerModel> abstractCustomerModelList) {
        this.abstractCustomerModelList = abstractCustomerModelList;
    }
    @Override
    public void processRow(ResultSet rs) throws SQLException {
        AbstractCustomerModel abstractCustomerModel = new AbstractCustomerModel();
        abstractCustomerModel.setId(rs.getInt("id"));
        abstractCustomerModel.setUserId(rs.getInt("user_id"));
        abstractCustomerModel.setFirstName(rs.getString("first_name"));
        abstractCustomerModel.setLastName(rs.getString("last_name"));
        abstractCustomerModel.setAddress(rs.getString("address"));
        abstractCustomerModel.setUsername(rs.getString("username"));
        abstractCustomerModel.setPassword(rs.getString("password"));
        abstractCustomerModel.setLoginTime(rs.getTimestamp("login_time"));
        abstractCustomerModel.setLogoutTime(rs.getTimestamp("logout_time"));
        abstractCustomerModel.setRole(Role.ROLE_CUSTOMER);
        abstractCustomerModel.setStatus(Status.fromOrdinal(rs.getInt("status")));
        abstractCustomerModel.setTimezone(rs.getString("timezone"));
        this.abstractCustomerModelList.add(abstractCustomerModel);
    }
}
