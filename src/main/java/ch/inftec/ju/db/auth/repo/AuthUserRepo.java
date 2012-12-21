package ch.inftec.ju.db.auth.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ch.inftec.ju.db.auth.entity.AuthUser;

/**
 * Spring repository interface for the User entity.
 * @author Martin
 *
 */
public interface AuthUserRepo extends CrudRepository<AuthUser, Long> {
	@Query("select u from AuthUser u where u.name=?1")
	AuthUser getByName(String name);
}
