package com.donato.fin_control_backend.core.ports.inbound;

import com.donato.fin_control_backend.core.domain.Tag;
import com.donato.fin_control_backend.core.domain.User;

import java.util.List;

public interface TagService {

    Tag createTag(String name, User user);

    Tag getTag(Long id, User user);

    List<Tag> getTags(User user);

    void deleteTag(Long id, User user);
}
