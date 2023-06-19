package com.project.reactor.debug;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

/**
 * This kind of Debug is for Line debug, Hooks.onOperatorDebug() it isn't advice by productions environments, so that have a lot of lifting process behind
 */
class DebugTest {


    @Test
    void onOperatorDebug() {
        //given
        Hooks.onOperatorDebug();

        //when
        var result = Flux.just("A", "B")
                .concatWith(Flux.error(new IllegalStateException()))
                .map(String::toLowerCase);

        //then
        StepVerifier.create(result)
                .expectNext("a", "b")
                .verifyComplete();

    }


    @Test
    void checkpointDebug() {
        //when
        var result = Flux.just("A", "B")
                .concatWith(Flux.error(new IllegalStateException()))
                .checkpoint("Error Generate By blablabla")
                .map(String::toLowerCase);

        //then
        StepVerifier.create(result)
                .expectNext("a", "b")
                .verifyComplete();

    }
}
