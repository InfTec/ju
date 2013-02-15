package ch.inftec.ju.db.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.db.auth.AuthenticationEditorViewModel.UserInfo.RoleInfo;
import ch.inftec.ju.db.auth.AuthenticationEditorViewModel.UserInfo.RoleState;
import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.util.event.AbstractViewModel;

/**
 * View model for the AuthenticationEditorModel.
 * @author Martin
 *
 */
public class AuthenticationEditorViewModel extends AbstractViewModel {
	@Autowired
	private AuthenticationEditorModel model;
	
	private UserInfo selectedUserInfo;
	private ObservableList<UserInfo> userInfos = ObservableCollections.observableList(new ArrayList<UserInfo>());
	
	@PostConstruct
	private void initUserInfos() {
		List<String> availableRoles = this.model.getAvailableRoles();
		for (AuthUser user : this.model.getUsers()) {
			UserInfo userInfo = new UserInfo(user);
			
			List<String> assignedRoles = this.model.getRoles(user);
			int shortId = 0;
			for (String role : availableRoles) {
				RoleState state = assignedRoles.contains(role)
						? RoleState.ASSIGNED
						: RoleState.UNASSIGNED;
				userInfo.addRoleInfo(role, shortId++, state);
			}
			
			this.userInfos.add(userInfo);			
		}
		
		this.selectedUserInfo = this.userInfos.size() > 0
				? this.userInfos.get(0)
				: null;
	}
	
	//FIXME Remove!
	public void createUser(String name) {
		AuthUser user = this.model.addUser(name);
		this.userInfos.add(new UserInfo(user));
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
			if (userInfo.hasChange()) {
				ArrayList<String> assignedRoles = new ArrayList<>();
				for (RoleInfo role : userInfo.getRoleInfos()) {
					if (role.getPlannedState() == RoleState.ASSIGNED) assignedRoles.add(role.getName());
				}
				
				this.model.setRoles(userInfo.user, assignedRoles);
			}
		}
		
		this.refresh();
	}
	
	public UserInfo getSelectedUserInfo() {
		return this.selectedUserInfo;
	}
	
	public ObservableList<UserInfo> getUserInfos() {
		return this.userInfos;
	}
	
	public static class UserInfo {
		private final AuthUser user;
		private ArrayList<RoleInfo> roleInfos = new ArrayList<>();
		
		private UserInfo(AuthUser user) {
			this.user = user;
		}
		
		private void addRoleInfo(String name, int shortId, RoleState currentState) {
			this.roleInfos.add(new RoleInfo(name, shortId, currentState));
		}
		
		public String getName() {
			return this.user.getName();
		}
				
		public List<RoleInfo> getRoleInfos() {
			return Collections.unmodifiableList(this.roleInfos);
		}
		
		public boolean hasChange() {
			for (RoleInfo roleInfo : this.getRoleInfos()) {
				if (roleInfo.hasChange()) return true;
			}
			return false;
		}		
		
		public enum RoleState {
			UNASSIGNED,
			ASSIGNED;
		}
		
		public static class RoleInfo {
			private final String name;
			private final int shortId;
			private RoleState currentState;
			private RoleState plannedState;
			
			public RoleInfo(String name, int shortId, RoleState currentState) {
				this.name = name;
				this.shortId = shortId;
				this.currentState = currentState;
				this.plannedState = currentState;
			}
			
			public String getName() {
				return this.name;
			}
			
			public int getShortId() {
				return this.shortId;
			}
			
			public RoleState getCurrentState() {
				return this.currentState;
			}
			
			public RoleState getPlannedState() {
				return this.plannedState;
			}
			
			public void setPlannedState(RoleState plannedState) {
				this.plannedState = plannedState;
			}
			
			public boolean hasChange() {
				return this.currentState != this.plannedState;
			}
		}
	}
}
