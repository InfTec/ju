package ch.inftec.ju.db.auth.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import ch.inftec.ju.db.AbstractPersistenceObject;

/**
 * Entity for a role. A role can belong to 0-n AuthUsers.
 * @author Martin
 *
 */
@Entity
public class AuthRole extends AbstractPersistenceObject implements Comparable<AuthRole> {
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(unique=true, nullable=false)
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

	@Override
	public int compareTo(AuthRole o) {
		return this.getName().compareTo(o.getName());
	}
}
