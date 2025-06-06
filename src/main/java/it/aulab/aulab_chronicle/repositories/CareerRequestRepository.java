package it.aulab.aulab_chronicle.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.aulab.aulab_chronicle.models.CareerRequest;
import it.aulab.aulab_chronicle.models.Role;
import it.aulab.aulab_chronicle.models.User;

public interface CareerRequestRepository extends CrudRepository<CareerRequest, Long> {

    List<CareerRequest> findByIsCheckedFalse();

    List<CareerRequest> findByIsAcceptedIsNull();

    List<CareerRequest> findByIsAcceptedTrue();

    List<CareerRequest> findByIsAcceptedFalse();

    boolean existsByUserAndRoleAndIsAcceptedIsNull(User user, Role role);

    @Query(value = "SELECT user_id FROM users_roles", nativeQuery = true)
    List<Long> findAllUserIds();

    @Query(value = "SELECT role_id FROM users_roles WHERE user_id = :id", nativeQuery = true)
    List<Long> findByUserId(@Param("id") Long id);

}
