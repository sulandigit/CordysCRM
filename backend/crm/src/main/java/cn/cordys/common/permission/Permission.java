package cn.cordys.common.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Permission Information
 * Data model representing a single permission entity
 *
 * @author jianxing
 */
@Data
@Schema(description = "Permission information")
public class Permission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Permission ID")
    private String id;
    
    @Schema(description = "Permission name")
    private String name;
    
    @Schema(description = "Whether this permission is enabled")
    private Boolean enable = false;
    
    @Schema(description = "Whether this is an enterprise-level permission")
    private Boolean license = false;
}