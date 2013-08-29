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

import ch.inftec.ju.db.auth.AuthenticationEditorViewModel.UserInfo;
import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.fx.property.MemoryBooleanProperty;
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
		private AuthenticationEditorModel authenticationEditorModel() {
			return new AuthenticationEditorModel();
		}
		
		@Bean
		private AuthenticationEditorViewModel authenticationEditorVieModel() {
			return new AuthenticationEditorViewModel();
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
	private AuthenticationEditorModel authModel;
	
	@Autowired
	private AuthenticationEditorViewModel authVm;
	
	@Test
	public void authenticationEditorModelTest() {
		this.createDbDataUtil().cleanImport("/datasets/auth/singleUser.xml");
		
		// Test the getUsers method
		List<AuthUser> u1 = this.authModel.getUsers();
		Assert.assertEquals(1, u1.size());
		Assert.assertEquals("user1", u1.get(0).getName());
		
		// Test the getUserNames method
		TestUtils.assertCollectionEquals(this.authModel.getUserNames(), "user1");
		// Check the role of the first user
		TestUtils.assertCollectionEquals(this.authModel.getRoles(u1.get(0)), "role1");
		
		// Add a new user
		AuthUser u2 = this.authModel.addUser("newUser", "password");
		TestUtils.assertCollectionEquals(this.authModel.getUserNames(), "newUser", "user1");
		
		// Make sure it doesn't have any roles
		Assert.assertEquals(0, this.authModel.getRoles(u2).size());
		
		// Add some roles
		this.authModel.setRoles(u2, Arrays.asList("role1", "newRole"));
		TestUtils.assertCollectionEquals(this.authModel.getRoles(u2), "newRole", "role1");
		
		// Change roles
		this.authModel.setRoles(u2, Arrays.asList("role1", "anotherRole"));
		TestUtils.assertCollectionEquals(this.authModel.getRoles(u2), "anotherRole", "role1");
		
		// Available roles
		TestUtils.assertCollectionConsistsOfAll(this.authModel.getAvailableRoles(), "role1", "newRole", "anotherRole");
		
		// Delete user
		this.authModel.deleteUser("newUser");
		TestUtils.assertCollectionEquals(this.authModel.getUserNames(), "user1");
		
		// Make sure deleting an unknown user will not cause problems
		this.authModel.deleteUser("unknownUser");
	}
	
	@Test
	public void authenticationEditorViewModelTest() {
		this.createDbDataUtil().cleanImport("/datasets/auth/singleUser.xml");
		
		this.authVm.refresh();
		
		// Read / modify the one existing user
		
		Assert.assertEquals(1, this.authVm.getUserInfos().size());
		UserInfo u1 = this.authVm.getUserInfos().get(0);
		Assert.assertEquals("user1", u1.getName());
		
		TestUtils.assertCollectionEquals(this.authVm.getUserInfos(), u1);
		Assert.assertEquals(3, u1.getRoles().size());		
		Assert.assertFalse(u1.hasChanged());
		
		MemoryBooleanProperty p1 = u1.getRoles().get("role1");
		Assert.assertTrue(p1.get());
		Assert.assertFalse(p1.hasChanged());
		Assert.assertFalse(this.authVm.hasRolesChanged());

		p1.set(false);
		Assert.assertTrue(p1.hasChanged());
		Assert.assertTrue(u1.hasChanged());
		Assert.assertTrue(this.authVm.hasRolesChanged());
		
		MemoryBooleanProperty p2 = u1.getRoles().get("newRole");
		p2.set(true);
		
		this.authVm.save();
		Assert.assertFalse(this.authVm.hasRolesChanged());
		
		UserInfo u1New = this.authVm.getUserInfos().get(0);
		Assert.assertFalse(u1New.getRoles().get("role1").get());
		Assert.assertTrue(u1New.getRoles().get("newRole").get());
	}
}
