package ch.inftec.ju.db.auth.entity;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;

import ch.inftec.ju.db.AbstractPersistenceObject;
import ch.inftec.ju.util.JuStringUtils;

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
	
	// Note: JPA only supports Set, but EclipseLink will allow TreeSet (as long as it
	// can instantiate it - SortedSet wouldn't work...)
	// We have to fetch eagerly, though...
	@ManyToMany(mappedBy="roles", fetch=FetchType.EAGER)
	@OrderBy("name")
	private TreeSet<AuthUser> users = new TreeSet<>();

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

	public SortedSet<AuthUser> getUsers() {
		return users;
	}

	@Override
	public int compareTo(AuthRole o) {
		return this.getName().compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "name", this.getName());
	}
}
