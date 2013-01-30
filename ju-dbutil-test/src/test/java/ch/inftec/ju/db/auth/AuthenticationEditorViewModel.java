package ch.inftec.ju.db.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.db.auth.AuthenticationEditorViewModel.UserInfo.RoleState;
import ch.inftec.ju.db.auth.entity.AuthUser;

/**
 * View model for the AuthenticationEditorModel.
 * @author Martin
 *
 */
public class AuthenticationEditorViewModel {
	@Autowired
	private AuthenticationEditorModel model;
	
	private UserInfo selectedUserInfo;
	private ArrayList<UserInfo> userInfos = new ArrayList<>();
	
	@PostConstruct
	private void initUserInfos() {
		for (AuthUser user : this.model.getUsers()) {
			UserInfo userInfo = new UserInfo(user);
			
			List<String> assignedRoles = this.model.getRoles(user);			
			for (String role : this.model.getAvailableRoles()) {
				RoleState state = assignedRoles.contains(role)
						? RoleState.ASSIGNED
						: RoleState.UNASSIGNED;
				userInfo.addRoleInfo(role, state);
			}
			
			this.userInfos.add(userInfo);			
		}
		
		this.selectedUserInfo = this.userInfos.size() > 0
				? this.userInfos.get(0)
				: null;
	}
	
	public UserInfo getSelectedUserInfo() {
		return this.selectedUserInfo;
	}
	
	public List<UserInfo> getUserInfos() {
		return Collections.unmodifiableList(this.userInfos);
	}
	
	public static class UserInfo {
		private final AuthUser user;
		private ArrayList<RoleInfo> roleInfos = new ArrayList<>();
		
		private UserInfo(AuthUser user) {
			this.user = user;
		}
		
		private void addRoleInfo(String name, RoleState currentState) {
			this.roleInfos.add(new RoleInfo(name, currentState));
		}
		
		public String getName() {
			return this.user.getName();
		}
		
		public List<RoleInfo> getRoleInfos() {
			return Collections.unmodifiableList(this.roleInfos);
		}
		
		public enum RoleState {
			UNASSIGNED,
			ASSIGNED;
		}
		
		public static class RoleInfo {
			private final String name;
			private RoleState currentState;
			private RoleState plannedState;
			
			public RoleInfo(String name, RoleState currentState) {
				this.name = name;
				this.currentState = currentState;
				this.plannedState = currentState;
			}
			
			public String getName() {
				return this.name;
			}
			
			public RoleState getCurrentState() {
				return this.currentState;
			}
			
			public RoleState getPlannedState() {
				return this.plannedState;
			}
		}
	}
}
