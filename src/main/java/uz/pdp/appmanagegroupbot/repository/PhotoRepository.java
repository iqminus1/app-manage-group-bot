package uz.pdp.appmanagegroupbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.appmanagegroupbot.enums.Status;
import uz.pdp.appmanagegroupbot.model.Photo;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findAllByStatus(Status status);
}