package com.example.schedulerdemo;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Component
@RequiredArgsConstructor
public class TestRouter {

    @Bean
    public RouterFunction<ServerResponse> testRouters(TestHandler testHandler){
        return RouterFunctions.route(GET("/test"), testHandler::test)
                .andRoute(GET("/local"), testHandler::localTest);
    }
}
