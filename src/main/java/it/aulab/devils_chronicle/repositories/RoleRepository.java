package it.aulab.devils_chronicle.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import it.aulab.devils_chronicle.models.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);

}
