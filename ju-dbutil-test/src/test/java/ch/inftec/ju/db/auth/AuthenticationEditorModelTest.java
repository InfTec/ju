package ch.inftec.ju.db.auth;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.util.TestUtils;

/**
 * Contains tests for the Authentication functionality.
 * @author Martin
 *
 */
@ContextConfiguration(classes={AuthenticationEditorModelTest.Configuration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthenticationEditorModelTest extends AbstractAuthBaseDbTest {
	static class Configuration {
		@Bean
		private AuthenticationEditorModel authenticationEditorViewModel() {
			return new AuthenticationEditorModel();
		}
		
		@Bean
		private RoleProvider roleProvider() {
			return new RoleProvider() {
				@Override
				public List<String> getAvailableRoles() {
					return Arrays.asList("role1", "newRole", "anotherRole");
				}
			};
		}
	}

	@Autowired
	private AuthenticationEditorModel authVm;
	
	@Test
	public void authenticationEditorModelTest() {
		this.loadDataSet("/datasets/auth/singleUser.xml");
		
		// Test the getUsers method
		List<AuthUser> u1 = this.authVm.getUsers();
		Assert.assertEquals(1, u1.size());
		Assert.assertEquals("user1", u1.get(0).getName());
		
		// Test the getUserNames method
		TestUtils.assertCollectionEquals(this.authVm.getUserNames(), "user1");
		// Check the role of the first user
		TestUtils.assertCollectionEquals(this.authVm.getRoles(u1.get(0)), "role1");
		
		// Add a new user
		AuthUser u2 = this.authVm.addUser("newUser");
		TestUtils.assertCollectionEquals(this.authVm.getUserNames(), "newUser", "user1");
		
		// Make sure it doesn't have any roles
		Assert.assertEquals(0, this.authVm.getRoles(u2).size());
		
		// Add some roles
		this.authVm.setRoles(u2, Arrays.asList("role1", "newRole"));
		TestUtils.assertCollectionEquals(this.authVm.getRoles(u2), "newRole", "role1");
		
		// Change roles
		this.authVm.setRoles(u2, Arrays.asList("role1", "anotherRole"));
		TestUtils.assertCollectionEquals(this.authVm.getRoles(u2), "anotherRole", "role1");
		
		// Available roles
		TestUtils.assertCollectionConsistsOfAll(this.authVm.getAvailableRoles(), "role1", "newRole", "anotherRole");
	}
}
