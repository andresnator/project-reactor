package com.project.reactor.debug;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.tools.agent.ReactorDebugAgent;

class DebugReactorInitTest {

    @BeforeEach
    void setUp() {
        ReactorDebugAgent.init();
        ReactorDebugAgent.processExistingClasses();
    }


    @Test
    void reactorDebugAgent() {
        //when
        var result = Flux.just("A", "B")
                .concatWith(Flux.error(new IllegalStateException()))
                .map(String::toLowerCase);

        //then
        StepVerifier.create(result)
                .expectNext("a", "b")
                .verifyComplete();

    }
}
