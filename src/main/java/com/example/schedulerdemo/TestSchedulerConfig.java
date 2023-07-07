package com.example.schedulerdemo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class TestSchedulerConfig {

    /**
     * CorePoolSize: 최초 동시에 시작되는 스레드 수
     * QueueCapacity: 동시에 시작되는 스레드 수의 한계치를 넘기게 되면 큐에 쌓이는 최대 허용량
     * MaxPoolSize: QueueCapacity의 한계치를 넘기게 되면, MaxPoolSize만큼 풀의 사이즈를 증가시킴.
     *
     * @return zenotaIOScheduler 반환
     */
    @Bean(name = "testIOScheduler")
    public Scheduler zenotaIOScheduler() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("test-io-pool-");
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(1200);
        executor.initialize();
        return Schedulers.fromExecutor(executor);
    }
}
