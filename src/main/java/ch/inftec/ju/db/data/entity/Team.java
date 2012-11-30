package ch.inftec.ju.db.data.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
public class Team {
	@Id
	@GeneratedValue
	private long id;
	
	private String name;
	
	private int ranking;
	
	@Temporal(TemporalType.DATE)
	private Date foundingDate;

	@Version
	private int version;
	
	@ManyToMany
	private Set<Player> players = new HashSet<>();
	
	public Set<Player> getPlayers() {
		return players;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public Date getFoundingDate() {
		return foundingDate;
	}

	public void setFoundingDate(Date foundingDate) {
		this.foundingDate = foundingDate;
	}
}