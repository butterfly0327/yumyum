package com.yumyumcoach.listener;

import com.yumyumcoach.config.DatabaseManager;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String url = getInitParameter(context, "db.url",
                "jdbc:mysql://localhost:3306/ssafy?serverTimezone=UTC&characterEncoding=UTF-8");
        String username = getInitParameter(context, "db.username", "ssafy");
        String password = getInitParameter(context, "db.password", "ssafy");
        DatabaseManager.getInstance().initialize(url, username, password);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DatabaseManager.getInstance().shutdown();
    }

    private String getInitParameter(ServletContext context, String name, String defaultValue) {
        String value = context.getInitParameter(name);
        return value != null ? value : defaultValue;
    }
}
