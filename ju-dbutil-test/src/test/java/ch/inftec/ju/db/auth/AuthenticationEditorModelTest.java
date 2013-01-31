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
import ch.inftec.ju.db.auth.AuthenticationEditorViewModel.UserInfo.RoleInfo;
import ch.inftec.ju.db.auth.AuthenticationEditorViewModel.UserInfo.RoleState;
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
		this.loadDataSet("/datasets/auth/singleUser.xml");
		
		// Test the getUsers method
		List<AuthUser> u1 = this.authModel.getUsers();
		Assert.assertEquals(1, u1.size());
		Assert.assertEquals("user1", u1.get(0).getName());
		
		// Test the getUserNames method
		TestUtils.assertCollectionEquals(this.authModel.getUserNames(), "user1");
		// Check the role of the first user
		TestUtils.assertCollectionEquals(this.authModel.getRoles(u1.get(0)), "role1");
		
		// Add a new user
		AuthUser u2 = this.authModel.addUser("newUser");
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
	}
	
	@Test
	public void authenticationEditorViewModelTest() {
		this.loadDataSet("/datasets/auth/singleUser.xml");
		
		this.authVm.refresh();
		
		// Read / modify the one existing user
		
		UserInfo u1 = this.authVm.getSelectedUserInfo();
		Assert.assertEquals("user1", u1.getName());
		
		TestUtils.assertCollectionEquals(this.authVm.getUserInfos(), u1);
		Assert.assertEquals(3, u1.getRoleInfos().size());		
		Assert.assertFalse(u1.hasChange());
		
		RoleInfo r1 = u1.getRoleInfos().get(0);
		Assert.assertEquals("role1", r1.getName());
		Assert.assertFalse(r1.hasChange());
		Assert.assertEquals(RoleState.ASSIGNED, r1.getCurrentState());
		Assert.assertEquals(RoleState.ASSIGNED, r1.getPlannedState());

		r1.setPlannedState(RoleState.UNASSIGNED);
		Assert.assertTrue(r1.hasChange());
		Assert.assertTrue(u1.hasChange());
		
		RoleInfo r2 = u1.getRoleInfos().get(1);
		r2.setPlannedState(RoleState.ASSIGNED);
		
		this.authVm.save();
		
		UserInfo u1New = this.authVm.getSelectedUserInfo();
		Assert.assertEquals(RoleState.UNASSIGNED, u1New.getRoleInfos().get(0).getCurrentState());
		Assert.assertEquals(RoleState.ASSIGNED, u1New.getRoleInfos().get(1).getCurrentState());
	}
}
