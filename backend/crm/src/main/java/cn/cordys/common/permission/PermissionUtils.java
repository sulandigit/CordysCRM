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
 * @author jianxing
 */
public class PermissionUtils {
    public static boolean hasPermission(String permission) {
        // Validate session first (fail fast)
        SessionUser user = SessionUtils.getUser();
        if (user == null) {
            throw new GenericException("User session not available");
        }
        String userId = SessionUtils.getUserId();
        String organizationId = OrganizationContext.getOrganizationId();

        // Admin bypass check (early exit)
        if (Strings.CS.equals(InternalUser.ADMIN.getValue(), user.getId())) {
            // admin 用户拥有所有权限
            return true;
        }

        // Fetch permissions only for non-admin users
        PermissionCache permissionCache = CommonBeanFactory.getBean(PermissionCache.class);
        if (permissionCache == null) {
            throw new GenericException("PermissionCache bean not available");
        }
        Set<String> permissionIds = permissionCache.getPermissionIds(userId, organizationId);

        // 判断是否拥有权限
        return permissionIds.contains(permission);
    }

    public static ResourceTabEnableDTO getTabEnableConfig(String userId, String permission, List<RolePermissionDTO> rolePermissions) {
        ResourceTabEnableDTO resourceTabEnableDTO = new ResourceTabEnableDTO();
        if (Strings.CS.equals(userId, InternalUser.ADMIN.getValue())) {
            resourceTabEnableDTO.setAll(true);
            resourceTabEnableDTO.setDept(true);
        }

        // Null-safe iteration
        if (CollectionUtils.isEmpty(rolePermissions)) {
            return resourceTabEnableDTO;
        }

        for (RolePermissionDTO rolePermission : rolePermissions) {
            if (rolePermission == null) {
                continue;
            }

            Set<String> permissions = Optional.ofNullable(rolePermission.getPermissions())
                    .orElse(Collections.emptySet());
            if (!permissions.contains(permission)) {
                // 判断权限
                continue;
            }
            if (Strings.CS.equalsAny(rolePermission.getDataScope(), RoleDataScope.ALL.name(), RoleDataScope.DEPT_CUSTOM.name())) {
                // 数据权限为全部或指定部门，显示所有tab
                resourceTabEnableDTO.setAll(true);
            }
            if (Strings.CS.equalsAny(rolePermission.getDataScope(), RoleDataScope.ALL.name(), RoleDataScope.DEPT_CUSTOM.name(),
                    RoleDataScope.DEPT_AND_CHILD.name())) {
                // 数据权限为全部或部门，显示部门tab
                resourceTabEnableDTO.setDept(true);
            }
        }
        return resourceTabEnableDTO;
    }
}