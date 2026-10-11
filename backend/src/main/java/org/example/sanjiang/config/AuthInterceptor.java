package org.example.sanjiang.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限拦截器 - 区分游客和用户的访问权限
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求（跨域预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // 定义游客可访问的路径（只读操作）
        if (isGuestAllowed(path, request.getMethod())) {
            return true;
        }

        // 需要登录才能访问的路径
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        // 检查是否为游客token
        if (token.startsWith("guest_")) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"游客模式无此权限，请登录后操作\"}");
            return false;
        }

        // 检查是否为有效用户token
        if (!token.startsWith("user_") && !token.startsWith("token_")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
            return false;
        }

        return true;
    }

    /**
     * 判断路径是否允许游客访问
     * 游客可以：查看列表、查看详情、搜索等只读操作
     * 游客不能：下单、收藏、发布、评论、修改个人信息等写操作
     */
    private boolean isGuestAllowed(String path, String method) {
        // AI对话接口允许POST请求
        if (path.startsWith("/api/ai") || path.startsWith("/api/chat")) {
            return true;
        }
        
        // 只有GET请求才允许游客访问其他接口
        if (!"GET".equalsIgnoreCase(method)) {
            return false;
        }

        // 允许游客访问的只读接口
        return path.startsWith("/api/attractions") ||      // 景点列表和详情
               path.startsWith("/api/heritage") ||         // 非遗列表和详情
               path.startsWith("/api/foods") ||            // 美食列表和详情
               path.startsWith("/api/hotels") ||           // 酒店列表和详情
               path.startsWith("/api/routes") ||           // 路线列表和详情
               path.startsWith("/api/tickets") ||          // 门票列表和详情
               path.startsWith("/api/posts") ||            // 帖子列表和详情
               path.startsWith("/api/moments") ||          // 社区动态列表和详情
               path.equals("/api/users/me");               // 获取用户信息（支持游客token）
    }
}