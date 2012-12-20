package ch.inftec.ju.db.auth.repo;

import org.springframework.data.repository.CrudRepository;

import ch.inftec.ju.db.auth.entity.AuthRole;

/**
 * Spring repository interface for the Role entity.
 * @author Martin
 *
 */
public interface AuthRoleRepo extends CrudRepository<AuthRole, Long>{
	AuthRole getByName(String name);
	AuthRole getByNameAndUsersId(String name, Long userId);
}
