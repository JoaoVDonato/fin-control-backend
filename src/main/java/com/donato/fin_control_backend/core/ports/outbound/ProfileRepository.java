package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
}
