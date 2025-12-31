package cn.cordys.crm.dashboard.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.dashboard.domain.DashboardModule;
import cn.cordys.crm.dashboard.dto.DashboardTreeNode;
import cn.cordys.crm.dashboard.dto.request.DashboardModuleAddRequest;
import cn.cordys.crm.dashboard.dto.request.DashboardModuleRenameRequest;
import cn.cordys.crm.dashboard.service.DashboardModuleService;
import cn.cordys.crm.system.dto.request.NodeMoveRequest;
import cn.cordys.security.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 仪表板模块控制器
 * 提供对仪表板模块的增删改查等操作
 */
@Tag(name = "仪表板模块")
@RestController
@RequestMapping("/dashboard/module")
public class DashboardModuleController {

    private static final String ADD_ENDPOINT = "/add";
    private static final String RENAME_ENDPOINT = "/rename";
    private static final String DELETE_ENDPOINT = "/delete";
    private static final String TREE_ENDPOINT = "/tree";
    private static final String COUNT_ENDPOINT = "/count";
    private static final String MOVE_ENDPOINT = "/move";

    @Resource
    private DashboardModuleService dashboardModuleService;

    /**
     * 添加仪表板模块
     * @param request 添加请求参数
     * @return 创建的仪表板模块
     */
    @PostMapping(DashboardModuleController.ADD_ENDPOINT)
    @RequiresPermissions(PermissionConstants.DASHBOARD_ADD)
    @Operation(summary = "仪表板-添加文件夹")
    public DashboardModule addFileModule(@Validated @RequestBody DashboardModuleAddRequest request) {
        return dashboardModuleService.addFileModule(
            request, 
            OrganizationContext.getOrganizationId(), 
            SessionUtils.getUserId()
        );
    }

    /**
     * 重命名仪表板模块
     * @param request 重命名请求参数
     */
    @PostMapping(DashboardModuleController.RENAME_ENDPOINT)
    @RequiresPermissions(PermissionConstants.DASHBOARD_EDIT)
    @Operation(summary = "仪表板-重命名文件夹")
    public void rename(@Validated @RequestBody DashboardModuleRenameRequest request) {
        dashboardModuleService.rename(request, SessionUtils.getUserId());
    }

    /**
     * 删除仪表板模块
     * @param ids 要删除的模块ID列表
     */
    @PostMapping(DashboardModuleController.DELETE_ENDPOINT)
    @RequiresPermissions(PermissionConstants.DASHBOARD_DELETE)
    @Operation(summary = "仪表板-刪除文件夹")
    public void deleteDashboardModule(@RequestBody @NotEmpty List<String> ids) {
        dashboardModuleService.delete(
            ids, 
            SessionUtils.getUserId(), 
            OrganizationContext.getOrganizationId()
        );
    }

    /**
     * 获取仪表板模块树形结构
     * @return 仪表板树节点列表
     */
    @GetMapping(DashboardModuleController.TREE_ENDPOINT)
    @Operation(summary = "仪表板-文件树查询")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    public List<DashboardTreeNode> getTree() {
        return dashboardModuleService.getTree(
            SessionUtils.getUserId(), 
            OrganizationContext.getOrganizationId()
        );
    }

    /**
     * 获取仪表板模块数量统计
     * @return 模块数量统计映射
     */
    @GetMapping(DashboardModuleController.COUNT_ENDPOINT)
    @Operation(summary = "仪表板-文件树数量")
    @RequiresPermissions(PermissionConstants.DASHBOARD_READ)
    public Map<String, Long> moduleCount() {
        return dashboardModuleService.moduleCount(
            SessionUtils.getUserId(), 
            OrganizationContext.getOrganizationId()
        );
    }

    /**
     * 移动仪表板节点
     * @param request 移动请求参数
     */
    @PostMapping(DashboardModuleController.MOVE_ENDPOINT)
    @Operation(summary = "仪表板-文件夹移动")
    @RequiresPermissions(PermissionConstants.DASHBOARD_EDIT)
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        dashboardModuleService.moveNode(request, SessionUtils.getUserId());
    }
}