package cn.cordys.listener;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Homepage controller class that handles requests to the root page ("/") and login page ("/login").
 * <p>
 * This controller is responsible for forwarding requests to the `index.html` page.
 * </p>
 * 
 * 主页控制器类,处理访问根页面("/")和登录页面("/login")的请求。
 * <p>
 * 该控制器负责将请求转发到 `index.html` 页面。
 * </p>
 */
@Controller
public class Index {
    /**
     * Handles requests to the web root path ("/web") and returns the homepage `index.html` page.
     * 
     * 处理根路径("/web")的请求,并返回首页 `index.html` 页面。
     *
     * @return Returns the view name of the homepage
     */
    @GetMapping("/web")
    public String index() {
        return "index.html";
    }

    /**
     * Handles requests to the mobile root path ("/mobile") and returns the mobile homepage `/mobile/index.html` page.
     * 
     * 处理移动端根路径("/mobile")的请求,并返回首页 `/mobile/index.html` 页面。
     *
     * @return Returns the view name of the mobile homepage
     */
    @GetMapping("/mobile")
    public String mobileIndex() {
        return "mobile/index.html";
    }


    /**
     * Handles requests to the login page ("/login") and returns the `index.html` page.
     * 
     * 处理登录页面("/login")的请求,并返回 `index.html` 页面。
     *
     * @return Returns the view name of the login page
     */
    @GetMapping(value = "/login")
    public String login() {
        return "/index.html";
    }
}
