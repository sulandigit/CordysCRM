package cn.cordys.listener;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 自定义错误控制器类，用于处理应用中的错误页面请求。
 * <p>
 * 该控制器会将所有错误页面的请求重定向到网站的根页面（"/"）。
 * </p>
 */
@Controller
public class CustomError implements ErrorController {

    /**
     * 错误处理方法，当发生错误时，会将请求重定向到根页面。
     * <p>
     * 该方法处理所有 HTTP 错误（如 404、500 等），通过 {@code @GetMapping("/error")} 注解
     * 自动映射到 Spring Boot 默认的错误处理路径。请求被重定向回应用的根路径。
     * </p>
     *
     * @return 重定向字符串，值为 "redirect:/"，将请求重定向到应用根页面
     */
    @GetMapping("/error")
    public String redirectRoot() {
        return "redirect:/";
    }
}