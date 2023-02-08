package com.star.counter.filter;

import com.google.common.collect.Sets;
import com.star.counter.service.api.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * @author zhaoyiming
 */
@Component
public class SessionCheckFilter implements Filter {

    @Autowired
    private AccountService accountService;

    private Set<String> whiteRootPaths = Sets.newHashSet("login", "msgsocket", "test");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //解决ajax跨域问题
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", "*");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String path = request.getRequestURI();
        String[] split = path.split("/");
        //白名单模式，解决页面访问权限问题（同时保证会话有效时间）
        if (split.length < 2) {
            request.getRequestDispatcher("/login/loginfail").forward(servletRequest, servletResponse);
        } else {
            if (!whiteRootPaths.contains(split[1])) {
                //缓存中存在用户token信息
                if (accountService.accountExistInCache(request.getParameter("token"))) {
                    //放行
                    filterChain.doFilter(servletRequest, servletResponse);
                } else {
                    request.getRequestDispatcher("/login/loginfail").forward(servletRequest, servletResponse);
                }
            } else {
                //放行
                filterChain.doFilter(servletRequest, servletResponse);
            }
        }
    }
}
