package ch.inftec.ju.db.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.JuDbUtils;
import ch.inftec.ju.db.auth.entity.AuthRole;
import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.db.auth.repo.AuthUserRepo;
import ch.inftec.ju.util.JuCollectionUtils;

/**
 * ViewModel to manage users and roles for Authentication Services.
 * <p>
 * This class works with the AuthUser and AuthRole entities.
 * @author Martin
 *
 */
public class AuthenticationEditorViewModel {
	@Autowired
	private DbConnection dbConn;
	
	/**
	 * Gets a list of all available users.
	 * @return List of users, sorted by the UserName
	 */
	public List<AuthUser> getUsers() {
		try {
			AuthUserRepo userRepo = JuDbUtils.getJpaRepository(this.dbConn.getEntityManager(), AuthUserRepo.class);
			return userRepo.findAll();
		} finally {
			this.dbConn.close();
		}
	}
	
	/**
	 * Gets all user names.
	 * @return List of user names, sorted alphabetically
	 */
	public List<String> getUserNames() {
		try {
			AuthUserRepo userRepo = JuDbUtils.getJpaRepository(this.dbConn.getEntityManager(), AuthUserRepo.class);
			return userRepo.findAllNames();
		} finally {
			this.dbConn.close();
		}
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
		try {
			this.dbConn.getEntityManager().persist(newUser);
			return newUser;
		} finally {
			this.dbConn.close();
		}
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
		
		try {
			AuthDao authDao = new AuthDao(this.dbConn.getEntityManager());
			
			for (String role : JuCollectionUtils.emptyForNull(roles)) {
				if (currentRoles.contains(role)) {
					// Just leave the role as is
					currentRoles.remove(role);
					continue;
				} else {
					// Add the new role
					authDao.addRole(user, role);
				}
			}
			
			// Remove any remaining role the user had previously
			for (String role : JuCollectionUtils.emptyForNull(currentRoles)) {
				authDao.removeRole(user, role);
			}
		} finally {
			this.dbConn.close();
		}
	}
}
