package ru.covenant.code.landing.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.covenant.code.landing.entity.AdminUser;
import ru.covenant.code.landing.repository.AdminUserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {
    private final AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AdminUser user = adminUserRepository.findByEmail(email)
                .orElseThrow(()-> {
                    log.error("Пользователь с таким email не найден");
                    return new UsernameNotFoundException("Пользователь не найден");
                });
        if(!user.isActive()){
            log.warn("Попытка входа для заблокированного пользователя");
            throw new DisabledException("Учетная запись заблокирована");
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
