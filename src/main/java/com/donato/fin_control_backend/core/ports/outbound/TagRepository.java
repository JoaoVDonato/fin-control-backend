package com.donato.fin_control_backend.core.ports.outbound;

import com.donato.fin_control_backend.infrastructure.entities.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

    List<TagEntity> findAllByUserId(Long userId);

    Optional<TagEntity> findByIdAndUserId(Long id, Long userId);

    Optional<TagEntity> findByNameAndUserId(String name, Long userId);
}
