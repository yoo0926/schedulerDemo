package com.example.schedulerdemo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestHandler {
    private final TestService testService;
    public Mono<ServerResponse> test(ServerRequest serverRequest) {
        List<Integer> list = List.of(1,2);
        return ServerResponse.ok()
                .body(testService.startTest(list), String.class);
    }

    public Mono<ServerResponse> localTest(ServerRequest serverRequest) {
        List<List<Integer>> totalList = new ArrayList<>();
        List<Integer> list1 = IntStream.range(1,20).boxed().collect(Collectors.toList());
        List<Integer> list2 = IntStream.range(1,20).boxed().collect(Collectors.toList());
        List<Integer> list3 = IntStream.range(1,20).boxed().collect(Collectors.toList());
        totalList.add(list1);
        totalList.add(list2);
        totalList.add(list3);
        return ServerResponse.ok()
                .body(testService.localTest(totalList), Integer.class);
    }
}
