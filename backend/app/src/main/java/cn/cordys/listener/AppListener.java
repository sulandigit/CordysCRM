package cn.cordys.listener;

import cn.cordys.common.service.DataInitService;
import cn.cordys.common.uid.impl.DefaultUidGenerator;
import cn.cordys.common.util.HikariCPUtils;
import cn.cordys.common.util.JSON;
import cn.cordys.common.util.LogUtils;
import cn.cordys.common.util.rsa.RsaKey;
import cn.cordys.common.util.rsa.RsaUtils;
import cn.cordys.crm.system.service.ExportTaskStopService;
import cn.cordys.crm.system.service.ExtScheduleService;
import cn.cordys.crm.system.service.SystemService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Application startup listener that handles initialization tasks after the application starts.
 * <p>
 * This component implements {@link ApplicationRunner} to execute configuration and initialization
 * procedures during the application startup phase, including UID generator setup, RSA key configuration,
 * scheduled task initialization, default organization data setup, and cache cleanup.
 * </p>
 */
@Component
class AppListener implements ApplicationRunner {
    // UID generator for generating unique identifiers
    @Resource
    private DefaultUidGenerator uidGenerator;

    // Service for managing scheduled tasks
    @Resource
    private ExtScheduleService extScheduleService;

    // Redis template for interacting with Redis storage
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // Service for initializing default data
    @Resource
    private DataInitService dataInitService;

    // Service for stopping export tasks
    @Resource
    private ExportTaskStopService exportTaskStopService;
    
    // Service for system-level operations
    @Resource
    private SystemService systemService;

    /**
     * 应用启动后执行的初始化方法。
     * Initialization method executed after application startup.
     * <p>
     * 此方法会依次初始化唯一 ID 生成器、MinIO 配置和 RSA 配置。
     * This method initializes the UID generator, RSA configuration, scheduled tasks,
     * default organization data, and performs necessary cleanup operations in sequence.
     * </p>
     *
     * @param args 启动参数 (startup arguments)
     */
    @Override
    public void run(ApplicationArguments args) {
        LogUtils.info("===== 开始初始化配置 =====");

        // 初始化唯一ID生成器
        // Initialize the unique ID generator
        uidGenerator.init();

        // 初始化RSA配置
        // Initialize RSA configuration
        LogUtils.info("初始化RSA配置");
        initializeRsaConfiguration();

        // Initialize scheduled tasks
        LogUtils.info("初始化定时任务");
        extScheduleService.startEnableSchedules();

        // Print HikariCP connection pool status
        HikariCPUtils.printHikariCPStatus();

        // Initialize default organization data
        LogUtils.info("初始化默认组织数据");
        dataInitService.initOneTime();

        // Stop all prepared export tasks
        LogUtils.info("停止导出任务");
        exportTaskStopService.stopPreparedAll();

        // Clear form cache
        LogUtils.info("清理表单缓存");
        systemService.clearFormCache();

        LogUtils.info("===== 完成初始化配置 =====");
    }

    /**
     * 初始化 RSA 配置。
     * Initialize RSA configuration.
     * <p>
     * 此方法首先尝试加载现有的 RSA 密钥。如果不存在，则生成新的 RSA 密钥并保存到文件系统。
     * This method first attempts to load existing RSA keys from Redis. If they don't exist,
     * it generates new RSA key pairs and saves them to Redis for future use.
     * </p>
     */
    private void initializeRsaConfiguration() {
        // Redis key for storing RSA key pair
        String redisKey = "rsa:key";
        try {
            // 从 Redis 获取 RSA 密钥
            // Retrieve RSA key from Redis
            String rsaStr = stringRedisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isNotBlank(rsaStr)) {
                // 如果 RSA 密钥存在，反序列化并设置密钥
                // If RSA key exists, deserialize and set the key
                RsaKey rsaKey = JSON.parseObject(rsaStr, RsaKey.class);
                RsaUtils.setRsaKey(rsaKey);
                return;
            }
        } catch (Exception e) {
            LogUtils.error("从 Redis 获取 RSA 配置失败", e);
        }

        try {
            // 如果 Redis 中没有密钥，生成新的 RSA 密钥并保存到 Redis
            // If no key exists in Redis, generate new RSA key pair and save to Redis
            RsaKey rsaKey = RsaUtils.getRsaKey();
            stringRedisTemplate.opsForValue().set(redisKey, JSON.toJSONString(rsaKey));
            RsaUtils.setRsaKey(rsaKey);
        } catch (Exception e) {
            LogUtils.error("初始化 RSA 配置失败", e);
        }
    }


}