package com.example.schedulerdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication
public class SchedulerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(TestService testService) {
        return (String[] args) -> {
            int defaultSize = Optional.ofNullable(System.getProperty("reactor.schedulers.defaultBoundedElasticSize"))
                    .map(Integer::parseInt)
                    .orElseGet(() -> 10 * Runtime.getRuntime().availableProcessors());

            log.info("DEFAULT_BOUNDED_ELASTIC_SIZE : {}", defaultSize);


            List<Integer> list = List.of(1,2);

            testService.startTest(list)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            value -> log.info("[Result1] Success : {}", value),
                            error -> log.error("[Result1] False : {}", error.getMessage())
                    );

            testService.startTest(list)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            value -> log.info("[Result2] Success : {}", value),
                            error -> log.error("[Result2] False : {}", error.getMessage())
                    );
        };
    }

}
