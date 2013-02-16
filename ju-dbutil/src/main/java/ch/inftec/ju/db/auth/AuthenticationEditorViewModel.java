package ch.inftec.ju.db.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.fx.property.MemoryBooleanProperty;
import ch.inftec.ju.fx.property.MemoryPropertyChangeTracker;
import ch.inftec.ju.util.event.AbstractViewModel;

/**
 * View model for the AuthenticationEditorModel.
 * @author Martin
 *
 */
public class AuthenticationEditorViewModel extends AbstractViewModel {
	@Autowired
	private AuthenticationEditorModel model;
	
	private List<String> availableRoles = new ArrayList<>();
	private ObservableList<UserInfo> userInfos = FXCollections.observableArrayList();
	
	private MemoryPropertyChangeTracker roleChangeTracker = new MemoryPropertyChangeTracker();
	
	@PostConstruct
	private void initUserInfos() {
		this.availableRoles = this.model.getAvailableRoles();
		this.roleChangeTracker.clear();
		
		for (AuthUser user : this.model.getUsers()) {
			this.addUserInfo(user);	
		}
	}
	
	private void addUserInfo(AuthUser user) {
		UserInfo userInfo = new UserInfo(user);
		
		List<String> assignedRoles = this.model.getRoles(user);
		for (String role : availableRoles) {
			MemoryBooleanProperty prop = userInfo.addRoleInfo(role, assignedRoles.contains(role));
			this.roleChangeTracker.addProperties(prop);
		}
		
		this.userInfos.add(userInfo);	
	}
	
	/**
	 * Refreshes the ViewModel, i.e. reloads all data from the DB.
	 */
	public void refresh() {
		this.userInfos.clear();
		this.initUserInfos();
	}
	
	/**
	 * Saves all changes that were made to the model.
	 */
	public void save() {
		for (UserInfo userInfo : this.getUserInfos()) {
			if (userInfo.hasChanged()) {
				ArrayList<String> assignedRoles = new ArrayList<>();
				for (String role : userInfo.getRoles().keySet()) {
					if (userInfo.getRoles().get(role).get()) {
						assignedRoles.add(role);
					}
				}
				
				this.model.setRoles(userInfo.user, assignedRoles);
			}
		}
		
		this.refresh();
	}
	
	public void createUser(String userName, String password) {
		AuthUser newUser = this.model.addUser(userName, password);
		addUserInfo(newUser);
	}
	
	public ObservableList<UserInfo> getUserInfos() {
		return this.userInfos;
	}
	
	public List<String> getAvailableRoles() {
		return this.availableRoles;
	}
	
	public BooleanProperty rolesChangedProperty() {
		return this.roleChangeTracker;
	}
	
	public boolean hasRolesChanged() {
		return this.roleChangeTracker.get();
	}
	
	public static class UserInfo {
		private final AuthUser user;
		private final Map<String, MemoryBooleanProperty> roles = new HashMap<>();
		
		private UserInfo(AuthUser user) {
			this.user = user;
		}
		
		private MemoryBooleanProperty addRoleInfo(String name, boolean assigned) {
			MemoryBooleanProperty prop = new MemoryBooleanProperty(assigned);
			this.roles.put(name, prop);
			return prop;
		}
		
		public String getName() {
			return this.user.getName();
		}
				
		public Map<String, MemoryBooleanProperty> getRoles() {
			return this.roles;
		}
		
		public boolean hasChanged() {
			for (MemoryBooleanProperty roleValue : this.getRoles().values()) {
				if (roleValue.hasChanged()) return true;
			}
			return false;
		}
	}
}
