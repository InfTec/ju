package ch.inftec.ju.db.auth;

import javax.persistence.EntityManager;

import ch.inftec.ju.db.JuDbUtils;
import ch.inftec.ju.db.auth.entity.AuthRole;
import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.db.auth.repo.AuthRoleRepo;

/**
 * Helper class for the AuthUser and AuthRole entities.
 * @author Martin
 *
 */
public class AuthDao {
	private EntityManager em;
	
	public AuthDao(EntityManager em) {
		this.em = em;
	}
	
	/**
	 * Adds the specified role to the User.
	 * <p>
	 * If the role doesn't exist yet, it is created automatically
	 * @param user Existing user
	 * @param roleName Role name
	 */
	public void addRole(AuthUser user, String roleName) {
		AuthRoleRepo roleRepo = JuDbUtils.getJpaRepository(this.em, AuthRoleRepo.class);
		
		// Check if the role exists
		AuthRole role = roleRepo.getByName(roleName);
		if (role == null) {
			role = new AuthRole();
			role.setName(roleName);
			this.em.persist(role);
		}
		
		// Check if the role has already been assigned to the user
		if (roleRepo.getByNameAndUsersId(roleName, user.getId()) == null) {
			// Role hasn't been assigned, so do it
			user.getRoles().add(role);
			role.getUsers().add(user);
		}
	}
}
