package org.hejinggang.elibrary.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Nacos配置中心管理器
 * 优先使用Nacos配置，若Nacos不可用则使用本地配置兜底
 */
@Component
public class NacosConfigManager {

    private static final Logger log = LoggerFactory.getLogger(NacosConfigManager.class);

    private final org.springframework.core.env.Environment environment;

    @Value("${nacos.server-addr:}")
    private String serverAddr;

    @Value("${nacos.namespace:}")
    private String namespace;

    @Value("${nacos.group:elibrary}")
    private String defaultGroup;

    private ConfigService configService;
    private boolean nacosAvailable = false;

    public NacosConfigManager(org.springframework.core.env.Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if (serverAddr != null && !serverAddr.isEmpty()) {
            try {
                java.util.Properties properties = new java.util.Properties();
                properties.put("serverAddr", serverAddr);
                if (namespace != null && !namespace.isEmpty()) {
                    properties.put("namespace", namespace);
                }
                configService = NacosFactory.createConfigService(properties);
                nacosAvailable = true;
                log.info("Nacos配置中心连接成功，使用Nacos配置: serverAddr={}", serverAddr);
            } catch (NacosException e) {
                nacosAvailable = false;
                log.warn("Nacos配置中心不可用，使用本地配置兜底: {}", e.getMessage());
            }
        } else {
            log.info("Nacos未配置，使用本地配置");
        }
    }

    public boolean isNacosAvailable() {
        return nacosAvailable;
    }

    public String getConfig(String dataId, String defaultValue) {
        return getConfig(dataId, defaultGroup, defaultValue);
    }

    public String getConfig(String dataId, String groupId, String defaultValue) {
        if (nacosAvailable && configService != null) {
            try {
                String config = configService.getConfig(dataId, groupId, 5000);
                if (config != null) {
                    return config;
                }
            } catch (NacosException e) {
                log.warn("从Nacos获取配置失败，使用本地配置: dataId={}, groupId={}", dataId, groupId);
            }
        }
        return environment.getProperty(dataId, defaultValue);
    }

    public int getIntConfig(String dataId, int defaultValue) {
        return getIntConfig(dataId, defaultGroup, defaultValue);
    }

    public int getIntConfig(String dataId, String groupId, int defaultValue) {
        String value = getConfig(dataId, groupId, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置值解析失败，使用默认值: dataId={}, value={}", dataId, value);
            return defaultValue;
        }
    }

    public BigDecimal getBigDecimalConfig(String dataId, String defaultValue) {
        String value = getConfig(dataId, defaultValue);
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("配置值解析失败，使用默认值: dataId={}, value={}", dataId, value);
            return new BigDecimal(defaultValue);
        }
    }

    public void addListener(String dataId, String groupId, ConfigChangeListener listener) {
        if (!nacosAvailable || configService == null) {
            log.warn("Nacos不可用，无法添加监听器: dataId={}, groupId={}", dataId, groupId);
            return;
        }
        try {
            configService.addListener(dataId, groupId, new Listener() {
                @Override
                public Executor getExecutor() {
                    return Executors.newSingleThreadExecutor();
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("Nacos配置变更: dataId={}, groupId={}", dataId, groupId);
                    listener.onConfigChange(configInfo);
                }
            });
            log.info("Nacos配置监听器添加成功: dataId={}, groupId={}", dataId, groupId);
        } catch (NacosException e) {
            log.error("添加Nacos监听器失败: dataId={}, groupId={}", dataId, groupId, e);
        }
    }

    public interface ConfigChangeListener {
        void onConfigChange(String configInfo);
    }
}