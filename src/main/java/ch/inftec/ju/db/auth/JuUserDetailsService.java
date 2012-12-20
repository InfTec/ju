package ch.inftec.ju.db.auth;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ch.inftec.ju.db.DbConnection;
import ch.inftec.ju.db.JuDbUtils;
import ch.inftec.ju.db.auth.UnknownUserHandler.NewUserInfo;
import ch.inftec.ju.db.auth.entity.AuthRole;
import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.db.auth.repo.AuthUserRepo;

/**
 * Custom implementation of the Spring UserDetailsService.
 * <p>
 * Can be used with the DaoAuthenticationProvider.
 * <p>
 * To use this service, Spring dependencies must be available:
 * <ul>
 *   <li>org.springframework.data:spring-data-jpa</li>
 *   <li>org.springframework.security:spring-security-core</li>
 *   <li>org.springframework.security:spring-security-config (when configuring by XML)</li>
 * </ul>
 * <p>
 * The class must run in a Swing Container that will inject a DbConnection dependency.
 * The service will close the connection when it's done with the lookup.
 * <p>
 * An optional UnknownUserHandler implementation can be injected to handle unknown users.
 * @author Martin
 *
 */
public class JuUserDetailsService implements UserDetailsService {
	@Autowired
	private DbConnection dbConn;
	
	@Autowired(required=false)
	private UnknownUserHandler unknownUserHandler;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			EntityManager em = this.dbConn.getEntityManager();
			
			AuthUserRepo authUserRepo = JuDbUtils.getJpaRepository(em, AuthUserRepo.class);
			AuthUser authUser = authUserRepo.getByName(username);
			
			if (authUser == null) {
				if (this.unknownUserHandler != null) {
					// Check whether the user should be added
					NewUserInfo newUserInfo = this.unknownUserHandler.handleUser(username);
					if (newUserInfo != null) {
						// Create the user
						authUser = new AuthUser();
						authUser.setName(username);
						authUser.setPassword(newUserInfo.getPassword());
						AuthUtil authUtil = new AuthUtil(em);
						for (String newAuth : newUserInfo.getAuthorities()) {
							authUtil.addRole(authUser, newAuth);
						}
						
						em.persist(authUser);
					}
				}
				
				if (authUser == null) {
					throw new UsernameNotFoundException("No such user: " + username);
				}
			}			
			
			List<GrantedAuthority> grantedAuths = new ArrayList<>();
			for (AuthRole authRole : authUser.getRoles()) {
				grantedAuths.add(new SimpleGrantedAuthority(authRole.getName()));
			}
			
			User user = new User(username, authUser.getPassword(), grantedAuths);
			
			return user;
		} finally {
			this.dbConn.close();
		}		
	}
}
