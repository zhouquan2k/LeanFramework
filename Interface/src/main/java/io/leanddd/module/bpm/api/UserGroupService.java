package io.leanddd.module.bpm.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface UserGroupService {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Group {
		String groupId;
		String groupName;
		String roleId;
		String orgId;
	}

	void createGroups(Group... groups);

	boolean groupExists(String groupId);
}
