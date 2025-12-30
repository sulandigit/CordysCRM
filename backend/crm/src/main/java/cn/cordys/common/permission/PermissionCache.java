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
 * 权限缓存
 *
 * @author jianxing
 */
@Component
public class PermissionCache {

    @Cacheable(value = "permission_cache", key = "#userId + ':' +  #orgId")
    public List<RolePermissionDTO> getRolePermissions(String userId, String orgId) {
        RoleService roleService = CommonBeanFactory.getBean(RoleService.class);
        if (roleService == null) {
            throw new GenericException("RoleService bean not available");
        }
        // 获取角色
        List<RoleDataScopeDTO> roleOptions = Optional.ofNullable(roleService.getRoleOptions(userId, orgId))
                .orElse(new ArrayList<>(0));
        List<String> roleIds = roleOptions.stream()
                .map(RoleDataScopeDTO::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>(0);
        }

        // 获取角色权限
        List<RolePermission> permissions = Optional.ofNullable(roleService.getPermissions(roleIds))
                .orElse(List.of());
        Map<String, List<RolePermission>> rolePermissionMap = permissions.stream()
                .collect(Collectors.groupingBy(RolePermission::getRoleId));

        // 缓存角色和权限
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

    public Set<String> getPermissionIds(String userId, String orgId) {
        PermissionCache permissionCache = CommonBeanFactory.getBean(PermissionCache.class);
        if (permissionCache == null) {
            throw new GenericException("PermissionCache bean not available");
        }
        List<RolePermissionDTO> rolePermissions = Optional.ofNullable(permissionCache.getRolePermissions(userId, orgId))
                .orElse(new ArrayList<>(0));
        return rolePermissions.stream()
                .flatMap(rolePermissionDTO -> Optional.ofNullable(rolePermissionDTO.getPermissions())
                        .orElse(Collections.emptySet()).stream())
                .collect(Collectors.toSet());
    }

    @CacheEvict(value = "permission_cache", key = "#userId + ':' +  #orgId", beforeInvocation = true)
    public void clearCache(String userId, String orgId) {
        // do nothing
    }
}