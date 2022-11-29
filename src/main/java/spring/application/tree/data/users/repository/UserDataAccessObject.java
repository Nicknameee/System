package spring.application.tree.data.users.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import spring.application.tree.data.exceptions.ApplicationException;
import spring.application.tree.data.exceptions.InvalidAttributesException;
import spring.application.tree.data.users.models.AbstractUserModel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserDataAccessObject {
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
        Optional<AbstractUserModel> abstractUserModelOptional =  userRepository.findById(id);
        return abstractUserModelOptional.orElse(null);
    }

    public boolean checkUsernameAvailability(String username) throws ApplicationException {
        if (username == null || username.isEmpty()) {
            throw new InvalidAttributesException(String.format("Username is invalid: %s", username),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        return userRepository.countAbstractUserModelsWithFollowingUsername(username) == 0;
    }

    public void saveUser(AbstractUserModel abstractUserModel) throws ApplicationException {
        validateUserModel(abstractUserModel);
        userRepository.save(abstractUserModel);
    }

    public void updateUser(AbstractUserModel abstractUserModel) throws InvalidAttributesException {
        validateUserModel(abstractUserModel);
        userRepository.save(abstractUserModel);
    }

    public void deleteUserById(Integer id) throws ApplicationException {
        if (id < 1) {
            throw new InvalidAttributesException(String.format("ID is invalid: %s", id),
                    Arrays.asList(Thread.currentThread().getStackTrace()).get(1).toString(),
                    LocalDateTime.now(), HttpStatus.NOT_ACCEPTABLE);
        }
        userRepository.deleteAbstractUserModelById(id);
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
