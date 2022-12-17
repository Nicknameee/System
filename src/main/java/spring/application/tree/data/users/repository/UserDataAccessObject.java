package spring.application.tree.data.users.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import spring.application.tree.data.exceptions.ApplicationException;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.users.attributes.Role;
import spring.application.tree.data.users.attributes.Status;
import spring.application.tree.data.users.models.AbstractCustomerModel;
import spring.application.tree.data.users.models.AbstractUserModel;
import spring.application.tree.data.users.repository.mappers.CustomerMapper;
import spring.application.tree.data.users.security.DataEncoderTool;
import spring.application.tree.data.utility.loaders.PropertyResourceLoader;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserDataAccessObject {
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    public AbstractUserModel getUserByLoginCredentials(String login) throws ApplicationException {
        if (login == null || login.isEmpty()) {
            throw new InvalidAttributesException(String.format("Username is invalid: %s", login),
                                                 Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                                                 LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        return userRepository.findUserByLogin(login);
    }

    public AbstractUserModel getUserById(Integer id) throws InvalidAttributesException {
        if (id == null || id < 1) {
            throw new InvalidAttributesException(String.format("User ID is invalid: %s", id),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        Optional<AbstractUserModel> abstractUserModelOptional = userRepository.findById(id);
        return abstractUserModelOptional.orElse(null);
    }

    public AbstractCustomerModel getCustomerById(Integer id) throws InvalidAttributesException {
        if (id == null || id < 1) {
            throw new InvalidAttributesException(String.format("Customer ID is invalid: %s", id),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String getCustomerByIDSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/users/getCustomerById.sql");
        log.debug("Get customer by ID query: {}", getCustomerByIDSQL);
        List<AbstractCustomerModel> abstractCustomerModels = new ArrayList<>();
        try {
            jdbcTemplate.query(getCustomerByIDSQL, new CustomerMapper(abstractCustomerModels), id);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
        return abstractCustomerModels.size() > 0 ? abstractCustomerModels.get(0) : null;
    }

    public boolean checkUsernameAvailability(String username) throws ApplicationException {
        if (username == null || username.isEmpty()) {
            throw new InvalidAttributesException(String.format("Username is invalid: %s", username),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        return userRepository.countAbstractUserModelsWithFollowingUsername(username) == 0;
    }

    public AbstractUserModel saveUser(AbstractUserModel abstractUserModel) throws ApplicationException {
        abstractUserModel.setPassword(DataEncoderTool.encodeData(abstractUserModel.getPassword()));
        validateUserModel(abstractUserModel);
        return userRepository.save(abstractUserModel);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void saveCustomer(AbstractCustomerModel abstractCustomerModel) throws InvalidAttributesException {
        abstractCustomerModel.setPassword(DataEncoderTool.encodeData(abstractCustomerModel.getPassword()));
        validateUserModel(abstractCustomerModel);
        String saveUserSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/users/saveUser.sql");
        String saveCustomerSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/users/saveCustomer.sql");
        log.debug("Save user query: {}", saveUserSQL);
        log.debug("Save customer query: {}", saveCustomerSQL);
        try {
            jdbcTemplate.update(saveUserSQL, abstractCustomerModel.getUsername(),
                                                                                 abstractCustomerModel.getPassword(),
                                                                                 Role.ROLE_CUSTOMER.ordinal(),
                                                                                 Status.ENABLED.ordinal(),
                                                                                 abstractCustomerModel.getTimezone());
            Integer userId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID();", Integer.class);
            jdbcTemplate.update(saveCustomerSQL, userId, abstractCustomerModel.getFirstName(), abstractCustomerModel.getLastName(), abstractCustomerModel.getAddress());
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    public AbstractUserModel updateUser(AbstractUserModel abstractUserModel) throws InvalidAttributesException {
        abstractUserModel.setPassword(DataEncoderTool.encodeData(abstractUserModel.getPassword()));
        validateUserModel(abstractUserModel);
        return userRepository.save(abstractUserModel);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateCustomer(AbstractCustomerModel abstractCustomerModel) throws InvalidAttributesException {
        abstractCustomerModel.setPassword(DataEncoderTool.encodeData(abstractCustomerModel.getPassword()));
        validateUserModel(abstractCustomerModel);
        String updateUserSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/users/updateUser.sql");
        String updateCustomerSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/users/updateCustomer.sql");
        log.debug("Update user query: {}", updateUserSQL);
        log.debug("Update customer query: {}", updateCustomerSQL);
        try {
            jdbcTemplate.update(updateUserSQL, abstractCustomerModel.getPassword(), abstractCustomerModel.getTimezone(), abstractCustomerModel.getUserId());
            jdbcTemplate.update(updateCustomerSQL, abstractCustomerModel.getFirstName(), abstractCustomerModel.getLastName(), abstractCustomerModel.getAddress(), abstractCustomerModel.getId());
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void deleteUserById(Integer id) throws ApplicationException {
        if (id < 1) {
            throw new InvalidAttributesException(String.format("ID is invalid: %s", id),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        userRepository.deleteAbstractUserModelById(id);
    }

    public void deleteCustomerByUserId(Integer id) throws InvalidAttributesException {
        if (id < 1) {
            throw new InvalidAttributesException(String.format("ID is invalid: %s", id),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        String deleteCustomerByIdSQL = PropertyResourceLoader.getSQLScript("classpath:/sql/users/deleteCustomerById.sql");
        log.debug("Delete customer by ID query: {}", deleteCustomerByIdSQL);
        try {
            jdbcTemplate.update(deleteCustomerByIdSQL, id);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void updateUserLoginTime(String username) throws InvalidAttributesException {
        if (username == null || username.isEmpty()) {
            throw new InvalidAttributesException(String.format("Username is invalid: %s", username),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        userRepository.updateUserLoginTimeByUsername(username);
    }

    public void updateUserLogoutTime(String username) throws InvalidAttributesException {
        if (username == null || username.isEmpty()) {
            throw new InvalidAttributesException(String.format("Username is invalid: %s", username),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        userRepository.updateUserLogoutTimeByUsername(username);
    }

    public void updateUserPassword(String login, String newPassword) throws InvalidAttributesException {
        if (login == null || login.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            throw new InvalidAttributesException(String.format("Login: %s or password is invalid: %s", login, newPassword),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        userRepository.updateUserPassword(login, newPassword);
    }

    private void validateUserModel(AbstractUserModel abstractUserModel) throws InvalidAttributesException {
        StringBuilder exceptionText = new StringBuilder();
        if (abstractUserModel.getUsername() == null || abstractUserModel.getUsername().isEmpty()) {
            exceptionText.append(String.format("Username is invalid: %s ", abstractUserModel.getUsername()));
        }
        if (abstractUserModel.getPassword() == null || abstractUserModel.getPassword().isEmpty()) {
            exceptionText.append(String.format("Password is invalid: %s ", abstractUserModel.getPassword()));
        }
        if (abstractUserModel.getTimezone() == null || abstractUserModel.getTimezone().isEmpty() || !ZoneId.getAvailableZoneIds().contains(abstractUserModel.getTimezone())) {
            exceptionText.append(String.format("Timezone is invalid: %s ", abstractUserModel.getTimezone()));
        }
        if (!exceptionText.toString().isEmpty()) {
            throw new InvalidAttributesException(exceptionText.toString(),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
