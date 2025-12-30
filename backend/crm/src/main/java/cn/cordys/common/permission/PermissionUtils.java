package cn.cordys.common.permission;

import cn.cordys.common.constants.InternalUser;
import cn.cordys.common.constants.RoleDataScope;
import cn.cordys.common.dto.ResourceTabEnableDTO;
import cn.cordys.common.dto.RolePermissionDTO;
import cn.cordys.common.util.CommonBeanFactory;
import cn.cordys.context.OrganizationContext;
import cn.cordys.security.SessionUser;
import cn.cordys.security.SessionUtils;
import org.apache.commons.lang3.Strings;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Permission Utilities
 * Static helper methods for permission checking and authorization
 *
 * @author jianxing
 */
public class PermissionUtils {
    /**
     * Check if the current user has the specified permission
     * Admin users automatically have all permissions
     *
     * @param permission Permission ID to check
     * @return true if user has permission, false otherwise
     * @throws GenericException if session is not available or beans cannot be retrieved
     */
    public static boolean hasPermission(String permission) {
        // Validate session first (fail fast approach)
        SessionUser user = SessionUtils.getUser();
        if (user == null) {
            throw new GenericException("User session not available");
        }
        String userId = SessionUtils.getUserId();
        String organizationId = OrganizationContext.getOrganizationId();

        // Admin bypass: admins have all permissions (early exit for performance)
        if (Strings.CS.equals(InternalUser.ADMIN.getValue(), user.getId())) {
            return true;
        }

        // Fetch permissions only for non-admin users
        PermissionCache permissionCache = CommonBeanFactory.getBean(PermissionCache.class);
        if (permissionCache == null) {
            throw new GenericException("PermissionCache bean not available");
        }
        Set<String> permissionIds = permissionCache.getPermissionIds(userId, organizationId);

        // Check if user's permission set contains the requested permission
        return permissionIds.contains(permission);
    }

    /**
     * Get tab enable configuration based on user permissions and role data scope
     * Determines which UI tabs (All/Dept) should be visible for the user
     *
     * @param userId User ID
     * @param permission Permission ID to check
     * @param rolePermissions List of user's role permissions with data scope
     * @return ResourceTabEnableDTO indicating which tabs are enabled
     */
    public static ResourceTabEnableDTO getTabEnableConfig(String userId, String permission, List<RolePermissionDTO> rolePermissions) {
        ResourceTabEnableDTO resourceTabEnableDTO = new ResourceTabEnableDTO();
        // Admin users can see all tabs
        if (Strings.CS.equals(userId, InternalUser.ADMIN.getValue())) {
            resourceTabEnableDTO.setAll(true);
            resourceTabEnableDTO.setDept(true);
        }

        // Null-safe iteration: early return if no role permissions
        if (CollectionUtils.isEmpty(rolePermissions)) {
            return resourceTabEnableDTO;
        }

        for (RolePermissionDTO rolePermission : rolePermissions) {
            // Skip null entries defensively
            if (rolePermission == null) {
                continue;
            }

            // Null-safe permission check with empty set fallback
            Set<String> permissions = Optional.ofNullable(rolePermission.getPermissions())
                    .orElse(Collections.emptySet());
            if (!permissions.contains(permission)) {
                // User doesn't have this permission in current role
                continue;
            }
            // Enable "All" tab for ALL or DEPT_CUSTOM data scope
            if (Strings.CS.equalsAny(rolePermission.getDataScope(), RoleDataScope.ALL.name(), RoleDataScope.DEPT_CUSTOM.name())) {
                resourceTabEnableDTO.setAll(true);
            }
            // Enable "Dept" tab for ALL, DEPT_CUSTOM, or DEPT_AND_CHILD data scope
            if (Strings.CS.equalsAny(rolePermission.getDataScope(), RoleDataScope.ALL.name(), RoleDataScope.DEPT_CUSTOM.name(),
                    RoleDataScope.DEPT_AND_CHILD.name())) {
                resourceTabEnableDTO.setDept(true);
            }
        }
        return resourceTabEnableDTO;
    }
}