package com.lifeos.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

/**
 * LifeOS 应用启动入口
 */
@SpringBootApplication(scanBasePackages = "com.lifeos")
@EnableScheduling
@MapperScan("com.lifeos.**.mapper")
public class LifeOsApplication {

    private static final Logger log = LoggerFactory.getLogger(LifeOsApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LifeOsApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }
        log.info("""
                ----------------------------------------------------------
                \tApplication '{}' is running! Access URLs:
                \tLocal: \t\t{}://localhost:{}{}
                \tExternal: \t{}://{}:{}{}
                \tHealth: \t{}://localhost:{}{}/api/health-check
                \tDoc: \t\t{}://localhost:{}{}/swagger-ui.html
                \tProfile(s): \t{}
                ----------------------------------------------------------""",
                env.getProperty("spring.application.name", "lifeos-backend"),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, serverPort, contextPath,
                protocol, serverPort, contextPath,
                env.getActiveProfiles().length == 0 ? env.getDefaultProfiles() : env.getActiveProfiles()
        );
    }
}

