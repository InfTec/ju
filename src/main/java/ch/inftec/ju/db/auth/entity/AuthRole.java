package ch.inftec.ju.db.auth.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * Entity for a role. A role can belong to 0-n AuthUsers.
 * @author Martin
 *
 */
@Entity
public class AuthRole {
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(unique=true)
	private String name;
	
	@ManyToMany(mappedBy="roles")
	private Set<AuthUser> users = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<AuthUser> getUsers() {
		return users;
	}
}