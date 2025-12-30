package cn.cordys.common.permission;

import cn.cordys.common.dto.RoleDataScopeDTO;
import cn.cordys.common.dto.RolePermissionDTO;
import cn.cordys.common.util.BeanUtils;
import cn.cordys.common.util.CommonBeanFactory;
import cn.cordys.crm.system.domain.RolePermission;
import cn.cordys.crm.system.service.RoleService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Permission Cache
 * Caches user role permissions to improve authorization performance
 *
 * @author jianxing
 */
@Component
public class PermissionCache {

    /**
     * Get role permissions for a user in an organization
     * Result is cached with key: userId:orgId
     *
     * @param userId User ID
     * @param orgId Organization ID
     * @return List of role permissions with data scope and permission IDs
     */
    @Cacheable(value = "permission_cache", key = "#userId + ':' +  #orgId")
    public List<RolePermissionDTO> getRolePermissions(String userId, String orgId) {
        // Retrieve RoleService bean with null safety check
        RoleService roleService = CommonBeanFactory.getBean(RoleService.class);
        if (roleService == null) {
            throw new GenericException("RoleService bean not available");
        }
        // Fetch user roles with null-safe fallback to empty list
        List<RoleDataScopeDTO> roleOptions = Optional.ofNullable(roleService.getRoleOptions(userId, orgId))
                .orElse(new ArrayList<>(0));
        List<String> roleIds = roleOptions.stream()
                .map(RoleDataScopeDTO::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>(0);
        }

        // Fetch role permissions with null-safe fallback
        List<RolePermission> permissions = Optional.ofNullable(roleService.getPermissions(roleIds))
                .orElse(List.of());
        Map<String, List<RolePermission>> rolePermissionMap = permissions.stream()
                .collect(Collectors.groupingBy(RolePermission::getRoleId));

        // Build cached role permissions by mapping role data to DTOs
        return roleOptions.stream()
                .map(roleDataScopeDTO -> {
                    RolePermissionDTO rolePermission = BeanUtils.copyBean(new RolePermissionDTO(), roleDataScopeDTO);
                    List<RolePermission> rolePermissions = rolePermissionMap.get(roleDataScopeDTO.getId());
                    if (CollectionUtils.isEmpty(rolePermissions)) {
                        rolePermission.setPermissions(Set.of());
                        return rolePermission;
                    }
                    rolePermission.setPermissions(
                            rolePermissions
                                    .stream()
                                    .map(RolePermission::getPermissionId)
                                    .collect(Collectors.toSet())
                    );
                    return rolePermission;
                }).collect(Collectors.toList());
    }

    /**
     * Get all permission IDs for a user by aggregating across all their roles
     * Uses self-reference via CommonBeanFactory to ensure @Cacheable proxy is invoked
     *
     * @param userId User ID
     * @param orgId Organization ID
     * @return Set of permission IDs
     */
    public Set<String> getPermissionIds(String userId, String orgId) {
        // Self-reference to trigger caching proxy
        PermissionCache permissionCache = CommonBeanFactory.getBean(PermissionCache.class);
        if (permissionCache == null) {
            throw new GenericException("PermissionCache bean not available");
        }
        // Retrieve role permissions with null-safe fallback
        List<RolePermissionDTO> rolePermissions = Optional.ofNullable(permissionCache.getRolePermissions(userId, orgId))
                .orElse(new ArrayList<>(0));
        // Flatten permissions from all roles with null-safe handling for each permission set
        return rolePermissions.stream()
                .flatMap(rolePermissionDTO -> Optional.ofNullable(rolePermissionDTO.getPermissions())
                        .orElse(Collections.emptySet()).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Clear cached permissions for a specific user and organization
     * Called when role assignments or permissions change
     *
     * @param userId User ID
     * @param orgId Organization ID
     */
    @CacheEvict(value = "permission_cache", key = "#userId + ':' +  #orgId", beforeInvocation = true)
    public void clearCache(String userId, String orgId) {
        // Cache eviction handled by annotation
    }
}