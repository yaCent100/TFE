package be.iccbxl.tfe.Driveshare.security;

import be.iccbxl.tfe.Driveshare.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CustomUserDetail implements UserDetails {

	private final User user;

	private List<GrantedAuthority> authorities;


	public CustomUserDetail(User user) {
		this.user = user;

		this.authorities = user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole()))
				.collect(Collectors.toList());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true; // Vous pouvez personnaliser
	}

	@Override
	public boolean isAccountNonLocked() {
		return true; // Vous pouvez personnaliser
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true; // Vous pouvez personnaliser
	}

	@Override
	public boolean isEnabled() {
		return true; // Assurez-vous que votre entité User a un champ 'active'
	}
}
