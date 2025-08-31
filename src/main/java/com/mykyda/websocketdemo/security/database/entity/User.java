package com.mykyda.websocketdemo.security.database.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@Getter
@Setter
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotEmpty(message = "Display Name shouldn`t be null or empty")
    @Builder.Default
    String displayName = "Anon";

    @NotEmpty(message = "tag shouldn`t be null or empty")
    @Column(unique = true)
    String tag;

    @Email
    @NotEmpty(message = "email shouldn`t be null")
    String email;

    @NotEmpty(message = "password shouldn`t be null or empty")
    String password;

    @Enumerated(EnumType.STRING)
    Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(this.role);
    }

    @Override
    public String getUsername() {
        return getEmail();
    }
}
