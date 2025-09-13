package es.marcha.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.marcha.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
