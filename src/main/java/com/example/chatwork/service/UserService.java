package com.example.chatwork.service;

import com.example.chatwork.entity.User;
import com.example.chatwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User.UserBuilder;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(username);
        builder.password(user.getPassword());
        builder.roles("USER"); // ロール（認可）は最低1つ必要
        return builder.build();
    }

    // 💾 Chatworkトークン保存など、既存メソッドも残してOK
    public void saveToken(String username, String token) {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setChatworkToken(token);
        repo.save(user);
    }

    public String getToken(String username) {
        return repo.findByUsername(username)
                .map(User::getChatworkToken)
                .orElse(null);
    }
}
