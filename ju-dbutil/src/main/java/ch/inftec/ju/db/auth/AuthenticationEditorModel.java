package ch.inftec.ju.db.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.auth.entity.AuthRole;
import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.db.auth.repo.AuthUserRepo;
import ch.inftec.ju.util.JuCollectionUtils;

/**
 * Model to manage users and roles for Authentication Services.
 * <p>
 * This class works with the AuthUser and AuthRole entities.
 * 
 * @author Martin
 *
 */
public class AuthenticationEditorModel {
//	@PersistenceContext
//	private EntityManager em;
	
	@Autowired
	private RoleProvider roleProvider;
	
	@Autowired
	private AuthUserRepo userRepo;
	
	@Autowired
	private AuthDao authDao;
	
	/**
	 * Gets a list of all available users.
	 * @return List of users, sorted by the UserName
	 */
	public List<AuthUser> getUsers() {
		return this.userRepo.findAll();
	}
	
	/**
	 * Gets all user names.
	 * @return List of user names, sorted alphabetically
	 */
	public List<String> getUserNames() {
		return this.userRepo.findAllNames();
	}
	
	/**
	 * Adds the specified user.
	 * @param userName
	 */
	public AuthUser addUser(String userName) {
		// Make sure the user doesn't exist yet
		if (this.getUser(userName) != null) {
			throw new JuDbException("User already exists: " + userName);
		}
		
		AuthUser newUser = new AuthUser();
		newUser.setName(userName);
		this.userRepo.save(newUser);
		
		return newUser;
	}
	
	/**
	 * Gets the AuthUser object for the specified user name.
	 * @param userName
	 * @return AuthUser instance or null if the user doesn't exist
	 */
	public AuthUser getUser(String userName) {
		// Make sure the user doesn't exist yet
		for (AuthUser user : this.getUsers()) {
			if (user.getName().equals(userName)) return user;
		}
		return null;
	}
	
	/**
	 * Gets all roles the specified user has.
	 * @param user
	 * @return List of role names
	 */
	public List<String> getRoles(AuthUser user) {
		List<String> roles = new ArrayList<>();
		for (AuthRole role : user.getRoles()) {
			roles.add(role.getName());
		}
		
		return roles;
	}
	
	/**
	 * Gets a list of all available roles.
	 * @return List of available roles
	 */
	public List<String> getAvailableRoles() {
		return this.roleProvider.getAvailableRoles();
	}

	/**
	 * Sets the specified roles for the user.
	 * <p>
	 * This method will remove any roles that the user currently has, but that are not
	 * specified in the roles list.
	 * @param user
	 * @param roles All roles the user should have, including the current roles that
	 * shouldn't be deleted
	 */
	public void setRoles(AuthUser user, List<String> roles) {
		List<String> currentRoles = this.getRoles(user);
		
		for (String role : JuCollectionUtils.emptyForNull(roles)) {
			if (currentRoles.contains(role)) {
				// Just leave the role as is
				currentRoles.remove(role);
				continue;
			} else {
				// Add the new role
				this.authDao.addRole(user, role);
			}
		}
		
		// Remove any remaining role the user had previously
		for (String role : JuCollectionUtils.emptyForNull(currentRoles)) {
			this.authDao.removeRole(user, role);
		}
	}
}