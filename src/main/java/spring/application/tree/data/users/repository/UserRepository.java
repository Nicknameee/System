package spring.application.tree.data.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import spring.application.tree.data.users.models.AbstractUserModel;

public interface UserRepository extends JpaRepository<AbstractUserModel, Integer> {
    @Query("SELECT user FROM AbstractUserModel user WHERE user.username = :login")
    AbstractUserModel findUserByLogin(@Param("login") String login);
    @Query(value = "SELECT COUNT(user) FROM AbstractUserModel user WHERE user.username = :username")
    Long countAbstractUserModelsWithFollowingUsername(@Param("username") String username);
    @Modifying
    @Transactional
    @Query("UPDATE AbstractUserModel u SET u.loginTime = current_timestamp WHERE u.username = :username")
    void updateUserLoginTimeByUsername(@Param("username") String username);
    @Modifying
    @Transactional
    @Query("UPDATE AbstractUserModel u SET u.logoutTime = current_timestamp WHERE u.username = :username")
    void updateUserLogoutTimeByUsername(@Param("username") String username);
    @Modifying
    @Transactional
    @Query("UPDATE AbstractUserModel u SET u.password = :password WHERE u.username = :login")
    void updateUserPassword(@Param("login") String login, @Param("password") String password);
    @Modifying
    @Transactional
    @Query("DELETE FROM AbstractUserModel user WHERE user.id = :id")
    void deleteAbstractUserModelById(@Param("id") Integer id);
}
