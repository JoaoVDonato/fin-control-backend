package com.donato.fin_control_backend.core.ports.inbound;

import com.donato.fin_control_backend.core.domain.Profile;
import com.donato.fin_control_backend.core.domain.User;
import com.donato.fin_control_backend.core.usecases.commands.CreateProfileCommand;
import com.donato.fin_control_backend.core.usecases.commands.UpdateProfileCommand;

public interface ProfileService {

    Profile createProfile(CreateProfileCommand command, User user);

    Profile updateProfile(UpdateProfileCommand command, User user);

    Profile getProfile(User user);

    void deleteProfile(User user);
}
