package uz.pdp.appmanagegroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.appmanagegroupbot.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllBySubscribedAndSubscriptionEndTimeIsBetween(boolean subscribed, LocalDateTime start, LocalDateTime end);

    List<User> findAllBySubscribed(boolean subscribed);

    List<User> findAllByAdminAfter(int admin);
}