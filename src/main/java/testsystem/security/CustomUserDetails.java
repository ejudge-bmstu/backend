package testsystem.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import testsystem.domain.User;

import java.util.Collection;
import java.util.Collections;


class CustomUserDetails implements UserDetails {

    private final User user;

    CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword_hash();
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return Collections.singletonList(user.getRole());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    String getRole() {
        return user.getRole().toString();
    }

}
