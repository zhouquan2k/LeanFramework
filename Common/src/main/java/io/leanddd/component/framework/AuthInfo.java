package io.leanddd.component.framework;

import java.io.Serializable;
import java.util.Set;

public interface AuthInfo extends Serializable {

    default String getUserId() {
        return getUsername();
    }

    String getUsername();

    default String getToken() {
        return null;
    }

    default String getPrimaryDepartment() {
        return null;
    }

    default Set<String> getDepartments() {
        return null;
    }

    default Set<String> getRoleNames() {
        return null;
    }

    default Set<String> getPermissions() {
        return null;
    }

    // json
    default String getUserOptions() { return null; }
}

