package de.hexfieldsstudio.hexfieldsdominion.account.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "username", nullable = false, length = 255, unique = true)
    private String username;
    @Column(name = "password", nullable = true, length = 255)
    private String password;
    @Column(name = "email", nullable = true, length = 255)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 255)
    private Role role;
    @Column(name = "refreshToken", nullable = true, length = 255)
    private String refreshToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    public void setPassword(String password, PasswordEncoder passwordEncoder) {
        this.setPassword(passwordEncoder.encode(password));
    }

    public void setRefreshToken(String refreshToken, PasswordEncoder passwordEncoder) {
        this.setRefreshToken(passwordEncoder.encode(refreshToken));
    }

    @Override
    public String getUsername() {
        return username;
    }

    // overwrite lombok generated methods that need to encode values
    public static class UserBuilder {
        private String password;
        private String refreshToken;

        public UserBuilder password(String password, PasswordEncoder passwordEncoder) {
            this.password = passwordEncoder.encode(password);
            return this;
        }

        public UserBuilder refreshToken(String refreshToken, PasswordEncoder passwordEncoder) {
            this.refreshToken = passwordEncoder.encode(refreshToken);
            return this;
        }
    }

}
