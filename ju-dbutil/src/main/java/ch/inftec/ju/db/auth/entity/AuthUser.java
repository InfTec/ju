package ch.inftec.ju.db.auth.entity;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import ch.inftec.ju.db.AbstractPersistenceObject;
import ch.inftec.ju.util.JuStringUtils;

/**
 * Entity for a User used for authentication.
 * <p>
 * A user can belong to 0-n AuthRoles.
 * @author Martin
 *
 */
@Entity
public class AuthUser extends AbstractPersistenceObject {
	@Id
	@GeneratedValue
	private Long id;

	@Column(unique=true)
	private String name;
	
	private String password;
	
	@ManyToMany
	private Set<AuthRole> roles = new TreeSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<AuthRole> getRoles() {
		return roles;
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "name", this.getName());
	}
}
