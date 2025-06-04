package it.aulab.aulab_chronicle.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import it.aulab.aulab_chronicle.models.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);

}
