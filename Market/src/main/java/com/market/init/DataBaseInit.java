package com.market.init;

import com.market.entity.Role;
import com.market.entity.User;
import com.market.repository.RoleRepository;
import com.market.repository.UserRepository;
import com.market.util.enums.RoleNameEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataBaseInit implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public DataBaseInit(RoleRepository roleRepository, UserRepository userRepository){
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        this.initRoles();
        this.createAdmin();
    }


    private void initRoles() {
        for (RoleNameEnum r : RoleNameEnum.values()) {
            if (roleRepository.findRoleByName(r) == null) {
                roleRepository.save(new Role(r));
            }
        }
    }


    private void createAdmin() {
        if (userRepository.getUserByUsername("Admin") == null) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            User user = new User("Admin",
                    "admin",
                    roleRepository.findRoleByName(RoleNameEnum.ADMIN),
                    "Admin",
                    "Adminov",
                    "tututu@abv.bg",
                    50, null, null,
                    true);
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            userRepository.save(user);
        }

    }
}
