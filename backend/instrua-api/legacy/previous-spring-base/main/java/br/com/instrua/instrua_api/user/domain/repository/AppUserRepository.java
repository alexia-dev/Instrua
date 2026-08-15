package br.com.instrua.instrua_api.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.instrua.instrua_api.user.controller.model.AppUser;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);
}