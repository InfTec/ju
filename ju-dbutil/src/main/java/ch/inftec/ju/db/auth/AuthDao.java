package ch.inftec.ju.db.auth;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.db.auth.entity.AuthRole;
import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.db.auth.repo.AuthRoleRepo;
import ch.inftec.ju.db.auth.repo.AuthUserRepo;

/**
 * Helper class for the AuthUser and AuthRole entities.
 * @author Martin
 *
 */
public class AuthDao {
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private AuthRoleRepo roleRepo;
	
	@Autowired
	private AuthUserRepo userRepo;
	
	/**
	 * Adds the specified role to the User.
	 * <p>
	 * If the role doesn't exist yet, it is created automatically
	 * @param user Existing user
	 * @param roleName Role name
	 */
	public void addRole(AuthUser user, String roleName) {
		// Check if the role exists
		AuthRole role = this.roleRepo.getByName(roleName);
		if (role == null) {
			role = new AuthRole();
			role.setName(roleName);
			this.em.persist(role);
		}
		
		// Check if the role has already been assigned to the user
		if (this.roleRepo.getByNameAndUsersId(roleName, user.getId()) == null) {
			// Role hasn't been assigned, so do it
			user.getRoles().add(role);
			role.getUsers().add(user);
			this.userRepo.save(user);
			this.roleRepo.save(role);
		}
	}
	
	/**
	 * Removes the specified role from the User.
	 * <p>
	 * If the user doesn't have the role or one of both doesn't exist, nothing is done.
	 * @param user
	 * @param roleName
	 */
	public void removeRole(AuthUser user, String roleName) {
		// Check if the role exists
		AuthRole role = this.roleRepo.getByName(roleName);
		if (role != null) {
			if (this.roleRepo.getByNameAndUsersId(roleName, user.getId()) != null) {
				// User has role assigned, so remove it
				role.getUsers().remove(user);
				user.getRoles().remove(role);
				this.roleRepo.save(role);
				this.userRepo.save(user);
			}
		}
	}
}
