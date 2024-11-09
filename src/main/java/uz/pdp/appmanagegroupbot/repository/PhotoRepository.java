package uz.pdp.appmanagegroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.pdp.appmanagegroupbot.enums.Status;
import uz.pdp.appmanagegroupbot.model.Photo;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findAllByStatus(Status status);

    long countByStatus(Status status);

    @Query("select count(distinct p.sendUserId) from Photo p where p.status = ?1")
    long countUniqueUsers(Status status);
}