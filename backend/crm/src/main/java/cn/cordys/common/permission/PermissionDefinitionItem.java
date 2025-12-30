package cn.cordys.common.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Permission Definition Item
 * Hierarchical menu structure for permission configuration UI
 * Supports recursive nesting for sub-menus
 *
 * @author jianxing
 */
@Data
@Schema(description = "Permission settings menu item")
public class PermissionDefinitionItem {
    @Schema(description = "Menu item ID")
    private String id;
    
    @Schema(description = "Menu item name")
    private String name;
    
    @Schema(description = "Whether this is an enterprise edition menu")
    private Boolean license = false;
    
    @Schema(description = "Whether all permissions in this menu are selected")
    private Boolean enable = false;
    
    @Schema(description = "List of permissions under this menu item")
    private List<Permission> permissions;
    
    @Schema(description = "Child sub-menu items (recursive structure)")
    private List<PermissionDefinitionItem> children;
}