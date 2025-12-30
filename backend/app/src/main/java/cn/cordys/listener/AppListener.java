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
    @Resource
    private DefaultUidGenerator uidGenerator;

    @Resource
    private ExtScheduleService extScheduleService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DataInitService dataInitService;

    @Resource
    private ExportTaskStopService exportTaskStopService;
    
    @Resource
    private SystemService systemService;

    /**
     * Initialization method executed after application startup.
     * <p>
     * This method initializes the UID generator, RSA configuration, scheduled tasks,
     * default organization data, and performs necessary cleanup operations in sequence.
     * </p>
     *
     * @param args startup arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        LogUtils.info("===== 开始初始化配置 =====");

        uidGenerator.init();

        LogUtils.info("初始化RSA配置");
        initializeRsaConfiguration();

        LogUtils.info("初始化定时任务");
        extScheduleService.startEnableSchedules();

        HikariCPUtils.printHikariCPStatus();

        LogUtils.info("初始化默认组织数据");
        dataInitService.initOneTime();

        LogUtils.info("停止导出任务");
        exportTaskStopService.stopPreparedAll();

        LogUtils.info("清理表单缓存");
        systemService.clearFormCache();

        LogUtils.info("===== 完成初始化配置 =====");
    }

    /**
     * Initialize RSA configuration.
     * <p>
     * This method first attempts to load existing RSA keys from Redis. If they don't exist,
     * it generates new RSA key pairs and saves them to Redis for future use.
     * </p>
     */
    private void initializeRsaConfiguration() {
        String redisKey = "rsa:key";
        try {
            String rsaStr = stringRedisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isNotBlank(rsaStr)) {
                RsaKey rsaKey = JSON.parseObject(rsaStr, RsaKey.class);
                RsaUtils.setRsaKey(rsaKey);
                return;
            }
        } catch (Exception e) {
            LogUtils.error("从 Redis 获取 RSA 配置失败", e);
        }

        try {
            RsaKey rsaKey = RsaUtils.getRsaKey();
            stringRedisTemplate.opsForValue().set(redisKey, JSON.toJSONString(rsaKey));
            RsaUtils.setRsaKey(rsaKey);
        } catch (Exception e) {
            LogUtils.error("初始化 RSA 配置失败", e);
        }
    }


}