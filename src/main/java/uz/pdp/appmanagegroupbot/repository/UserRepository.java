package uz.pdp.appmanagegroupbot.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.appmanagegroupbot.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllBySubscribedAndSubscriptionEndTimeIsBetween(boolean subscribed, LocalDateTime start, LocalDateTime end);

    Page<User> findAllByBlocked(Pageable pageable, boolean blocked);


    Page<User> findAllBySubscribedAndBlocked(boolean subscribed, Pageable pageable, boolean blocked);

    Page<User> findAllBySubscribed(boolean subscribed, Pageable pageable);

    long countByBlocked(boolean blocked);
}