package com.donato.fin_control_backend.core.usecases;

import com.donato.fin_control_backend.adapters.api.http.handler.ResourceNotFoundException;
import com.donato.fin_control_backend.core.domain.Tag;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.ports.inbound.TagService;
import com.donato.fin_control_backend.core.ports.outbound.TagRepository;
import com.donato.fin_control_backend.core.ports.outbound.UserRepository;
import com.donato.fin_control_backend.infrastructure.entities.TagEntity;
import com.donato.fin_control_backend.infrastructure.entities.UserEntity;
import com.donato.fin_control_backend.infrastructure.mappers.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Tag createTag(String name, User user) {
        UserEntity userEntity = resolveUser(user);
        TagEntity tag = TagEntity.builder()
                .user(userEntity)
                .name(name)
                .createdAt(LocalDateTime.now())
                .build();
        return TagMapper.toDomain(tagRepository.save(tag));
    }

    @Override
    public Tag getTag(Long id, User user) {
        UserEntity userEntity = resolveUser(user);
        return TagMapper.toDomain(tagRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found")));
    }

    @Override
    public List<Tag> getTags(User user) {
        UserEntity userEntity = resolveUser(user);
        return tagRepository.findAllByUserId(userEntity.getId()).stream()
                .map(TagMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteTag(Long id, User user) {
        UserEntity userEntity = resolveUser(user);
        TagEntity tag = tagRepository.findByIdAndUserId(id, userEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        tagRepository.delete(tag);
    }

    private UserEntity resolveUser(User user) {
        if (user.getId() != null) {
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }
        return userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
