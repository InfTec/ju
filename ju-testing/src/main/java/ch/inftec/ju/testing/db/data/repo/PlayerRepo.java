package ch.inftec.ju.testing.db.data.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ch.inftec.ju.testing.db.data.entity.Player;
import ch.inftec.ju.testing.db.data.entity.TestingEntity;

/**
 * Spring data CrudRepository for the Player entity.
 * @author Martin
 *
 */
public interface PlayerRepo extends CrudRepository<Player, Long> {
	/*
	 * EclipseLink has a problem when the query should be created dynamically. When
	 * we explicitly define the query, we don't have this problem.
	 * Hibernate seems to work in both cases.
	 */
	@Query("select t from Player p where p.lastName = ?1")
	TestingEntity getByLastName(String name);
}
