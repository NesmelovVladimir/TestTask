package com.example.test.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
public class User implements UserDetails {

    @Serial
    private static final long serialVersionUID = -27094297857876111L;

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Transient
    private Collection<? extends GrantedAuthority> authorities;

    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = AuthorityUtils.createAuthorityList("USER");
        }
        return authorities;
    }
}
