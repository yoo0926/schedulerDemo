package com.example.schedulerdemo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestService {
    @Resource(name = "testIOScheduler")
    private Scheduler testIOScheduler;

    public Mono<String> startTest(List<Integer> list) {
        return Flux.fromIterable(list)
                .parallel(list.size())
                .runOn(Schedulers.boundedElastic())
                .flatMap(num -> {
                    log.info("[Main-1] parallel-{} : {}", num, Thread.currentThread().getName());
                    return serviceHour(num)
                            .doOnNext(aBoolean -> {
                                if (aBoolean) {
                                    log.error("[Main] serviceHour-{} : {}", num, Thread.currentThread().getName());
                                    throw new RuntimeException("test");
                                }
                            })
                            .flatMap(isValid -> listAll(num)
                                    .collectList()
                                    .map(resList -> {
                                        log.info("[Main] collectList : {}", Thread.currentThread().getName());
                                        return String.join(" || ", resList);
                                    }));
                })
                .sequential()
                .collectList()
                .map(resultList -> {
                    log.info("[Main] CollectList : {}", Thread.currentThread().getName());
                    return String.join(", ", resultList);
                })
                .timeout(Duration.ofMillis(5000), Mono.defer(() -> {
                    log.warn("[Main] timeout 5000 : {}", Thread.currentThread().getName());
                    return Mono.just("Success but timeout");
                }))
                .onErrorResume(throwable -> {
                    log.error("[Main] onErrorResume : {}", throwable.getMessage());
                    return Mono.error(new RuntimeException("False"));
                });
    }

    public Mono<Integer> localTest(List<List<Integer>> totalList) {
        return Flux.fromIterable(totalList)
                .parallel(totalList.size())
                .runOn(Schedulers.boundedElastic())
                .flatMap(list -> {
                    log.info("[Main-1] parallel-1 : {}", Thread.currentThread().getName());
                    return Flux.fromIterable(list)
                            .parallel(list.size() / 2)
                            .runOn(Schedulers.boundedElastic())
                            .flatMap(num -> {
                                log.error("[Main] parallel-1 : {}", Thread.currentThread().getName());
                                return Mono.fromCallable(() -> {
                                            if (num > 19) {
                                                log.warn("Timeout - {}", num);
                                                TimeUnit.SECONDS.sleep(10);
                                            }
                                            return num * 10;
                                        })
                                        .publishOn(Schedulers.boundedElastic())
                                        .map(num10 -> num10 / 10)
                                        .timeout(Duration.ofMillis(4000));
                            })
                            .sequential()
                            .collectList()
                            .onErrorContinue((throwable, o) -> {
                                log.error("[onErrorContinue] {} : {}", Thread.currentThread().getName(), throwable.getMessage());
                            })
                            .map(integers -> {
                                return integers.stream().mapToInt(Integer::intValue).sum();
                            });
                })
                .sequential()
                .collectList()
                .map(integers -> integers.stream().mapToInt(Integer::intValue).sum())
                .timeout(Duration.ofMillis(5000), Mono.defer(() -> {
                    log.warn("[Main] timeout 5000 : {}", Thread.currentThread().getName());
                    return Mono.just(9999);
                }))
                .onErrorResume(throwable -> {
                    log.error("[Main] onErrorResume : {}", throwable.getMessage());
                    return Mono.error(new RuntimeException("False"));
                });
    }

    public Mono<Boolean> serviceHour(int num) {
        return Mono.fromCallable(() -> {
            log.info("[serviceHour] Num : {} , : {}", num, Thread.currentThread().getName());
            TimeUnit.MILLISECONDS.sleep(500);
            return num % 2 == 1;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<String> listAll(int num) {
        List<String> airlineList = List.of("A", "B", "C", "D", "E", "F");
        String fakeUrl = "https://jsonplaceholder.typicode.com";
        String fakeUrl2 = "https://httpbin.org/get";
        String fakeUrl3 = "https://reqres.in/api/users?page=2";
        return Flux.fromIterable(airlineList)
                .flatMap(airlineCode -> {
                    switch (airlineCode) {
                        case "A":
                        case "B": {
                            return callWebClientTest(fakeUrl, num, true, false)
                                    .map(res -> "Success");
                        }
                        case "C": {
                            return callWebClientTest(fakeUrl2, num, false, false)
                                    .map(res -> "False");
                        }
                        case "D":
                        case "E": {
                            return callWebClientTest(fakeUrl3, num, true, true)
                                    .map(res -> "Success");
                        }
                        case "F": {
                            return callWebClientTest(fakeUrl2, num, false, true)
                                    .map(res -> "False");
                        }
                        default:
                            return Mono.error(new RuntimeException("undefined"));
                    }
                })
                .onErrorContinue((throwable, o) -> {
                    log.error("[listAll-{}-Error] {}", num, throwable.getMessage(), throwable);
                });
    }

    private Mono<String> callWebClientTest(String fakeUrl, int num, boolean isSuccess, boolean isParallel) {
        if (isParallel) {
            List<Integer> list = List.of(1, 2);
            return Flux.fromIterable(list)
                    .parallel(list.size())
                    .runOn(Schedulers.boundedElastic())
                    .flatMap(count -> {
                        log.info("[callWebClientTest-parallel-{} {} : {}]", count, num, Thread.currentThread().getName());
                        return getWebClient(fakeUrl, num, isSuccess);
                    })
                    .sequential()
                    .collectList()
                    .map(resultList -> String.join(", ", resultList))
                    .timeout(Duration.ofMillis(4000));
        } else {
            return getWebClient(fakeUrl, num, isSuccess)
                    .timeout(Duration.ofMillis(4000));
        }
    }

    private Mono<String> getWebClient(String fakeUrl, int num, boolean isSuccess) {
        int timeout = isSuccess ? 0 : 5;
        return WebClient.create(fakeUrl)
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(s -> {
                    try {
                        log.info("[getWebClient-WebClient-bodyToMono-{} : {}]", num, Thread.currentThread().getName());
                        TimeUnit.SECONDS.sleep(timeout);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(s -> {
                    log.info("[getWebClient-WebClient-publishOn-{} : {}]", num, Thread.currentThread().getName());
                })
                .doOnSuccess(res -> {
                    // logging
                    Mono.fromRunnable(() -> {
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                    log.error("[getWebClient-Logging-Success-{} Error] {}", num, e.getMessage());
                                }
                            })
                            .subscribeOn(testIOScheduler).subscribe();
                })
                .doOnError(throwable -> {
                    // logging
                    Mono.fromRunnable(() -> {
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException e) {
                                    log.error("[getWebClient-Logging-Fail-{} Error] {}", num, e.getMessage());
                                }
                            })
                            .subscribeOn(testIOScheduler).subscribe();
                })
                .onErrorResume(throwable -> {
                    log.error("[getWebClient-onErrorResume-{}] {}", num, throwable.getMessage());
                    return Mono.error(new RuntimeException("error.communication", throwable));
                });
    }
}
