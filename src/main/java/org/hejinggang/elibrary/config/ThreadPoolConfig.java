package org.hejinggang.elibrary.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import jakarta.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * 线程池配置
 * 支持通过Nacos动态调整线程池参数
 */
@Configuration
public class ThreadPoolConfig {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolConfig.class);

    @Value("${nacos.server-addr:}")
    private String serverAddr;

    @Value("${nacos.namespace:}")
    private String namespace;

    private ConfigService configService;
    private boolean nacosAvailable = false;

    // 线程池实例
    private ThreadPoolExecutor borrowPool;
    private ThreadPoolExecutor paymentPool;
    private ThreadPoolExecutor notificationPool;
    private ThreadPoolExecutor reconciliationPool;
    private ThreadPoolExecutor esSyncPool;
    private ThreadPoolExecutor balanceDeductPool;
    private ThreadPoolExecutor forceReturnPool;
    private ThreadPoolExecutor bookParsePool;

    @PostConstruct
    public void init() {
        if (serverAddr != null && !serverAddr.isEmpty()) {
            try {
                Properties properties = new Properties();
                properties.put("serverAddr", serverAddr);
                if (namespace != null && !namespace.isEmpty()) {
                    properties.put("namespace", namespace);
                }
                configService = NacosFactory.createConfigService(properties);
                nacosAvailable = true;
                log.info("Nacos配置中心连接成功，支持线程池动态配置");

                // 监听线程池配置变更
                configService.addListener("threadpool-config", "elibrary", new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return Executors.newSingleThreadExecutor();
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        log.info("线程池配置变更: {}", configInfo);
                        // TODO: 解析配置并更新线程池参数
                    }
                });
            } catch (NacosException e) {
                nacosAvailable = false;
                log.warn("Nacos配置中心不可用，使用固定线程池配置: {}", e.getMessage());
            }
        }
    }

    @Bean("borrowPool")
    public ThreadPoolExecutor borrowPool() {
        borrowPool = new ThreadPoolExecutor(
                10, 50, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new CustomizableThreadFactory("borrow-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return borrowPool;
    }

    @Bean("paymentPool")
    public ThreadPoolExecutor paymentPool() {
        paymentPool = new ThreadPoolExecutor(
                20, 100, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000),
                new CustomizableThreadFactory("payment-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return paymentPool;
    }

    @Bean("notificationPool")
    public ThreadPoolExecutor notificationPool() {
        notificationPool = new ThreadPoolExecutor(
                5, 20, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new CustomizableThreadFactory("notification-pool-"),
                new ThreadPoolExecutor.DiscardPolicy()
        );
        return notificationPool;
    }

    @Bean("reconciliationPool")
    public ThreadPoolExecutor reconciliationPool() {
        reconciliationPool = new ThreadPoolExecutor(
                5, 10, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new CustomizableThreadFactory("reconciliation-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return reconciliationPool;
    }

    @Bean("esSyncPool")
    public ThreadPoolExecutor esSyncPool() {
        esSyncPool = new ThreadPoolExecutor(
                10, 30, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new CustomizableThreadFactory("es-sync-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return esSyncPool;
    }

    @Bean("balanceDeductPool")
    public ThreadPoolExecutor balanceDeductPool() {
        balanceDeductPool = new ThreadPoolExecutor(
                10, 50, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new CustomizableThreadFactory("balance-deduct-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return balanceDeductPool;
    }

    @Bean("forceReturnPool")
    public ThreadPoolExecutor forceReturnPool() {
        forceReturnPool = new ThreadPoolExecutor(
                5, 20, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new CustomizableThreadFactory("force-return-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return forceReturnPool;
    }

    @Bean("bookParsePool")
    public ThreadPoolExecutor bookParsePool() {
        bookParsePool = new ThreadPoolExecutor(
                5, 20, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new CustomizableThreadFactory("book-parse-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        return bookParsePool;
    }
}