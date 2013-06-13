package ch.inftec.ju.db.auth.entity;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
public class AuthUser extends AbstractPersistenceObject implements Comparable<AuthUser> {
	@Id
	@GeneratedValue
	private Long id;

	@Column(unique=true, nullable=false)
	private String name;
	
	private String password;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLogin;
	
	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Integer getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(Integer loginCount) {
		this.loginCount = loginCount;
	}

	private Integer loginCount;
	
	// Note: JPA only supports Set, but EclipseLink will allow TreeSet (as long as it
	// can instantiate it - SortedSet wouldn't work...)
	// We have to fetch eagerly, though...
	// XXX Still the same with Hibernate??
	@ManyToMany(fetch=FetchType.EAGER)
	@OrderBy("name")	
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
	public int compareTo(AuthUser o) {
		return this.getName().compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "name", this.getName());
	}
}
