package cn.cordys.config;

import cn.cordys.common.util.LogUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 50;
    private static final int QUEUE_CAPACITY = 500;
    private static final int KEEP_ALIVE_SECONDS = 60;
    private static final int AWAIT_TERMINATION_SECONDS = 60;

    @Bean(name = {"threadPoolTaskExecutor", "applicationTaskExecutor"})
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setThreadNamePrefix("cs-async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        executor.setAwaitTerminationMillis(0);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            StringBuilder sb = new StringBuilder("异步任务异常: [方法名: ")
                .append(method.getName())
                .append(", 异常信息: ")
                .append(ex.getMessage());
            if (params != null && params.length > 0) {
                sb.append(", 参数数量: ").append(params.length);
            }
            sb.append("]");
            LogUtils.error(sb.toString(), ex);
        };
    }
}