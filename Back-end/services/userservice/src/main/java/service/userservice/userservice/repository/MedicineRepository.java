package service.userservice.userservice.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import service.userservice.userservice.model.Medicine;
@Repository public interface MedicineRepository extends JpaRepository<Medicine, String> {}