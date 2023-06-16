package com.project.reactor.scheduler;

import static com.project.reactor.utils.CommonUtils.delay;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

/**
 * <p>
 * Sure! I'll provide an explanation and some use cases for `Schedulers.parallel()` and `Schedulers.boundedElastic()` in Reactor.
 *
 * 1. `Schedulers.parallel()`: This Scheduler provides a pool of parallel threads for parallel execution of tasks. Here's an explanation and
 * some use cases:
 *
 * - Explanation: `Schedulers.parallel()` creates a pool of parallel threads, which means multiple tasks can be executed simultaneously on
 * different threads. The number of parallel threads is determined by the processor's capacity and will be automatically adjusted based on
 * the workload.
 *
 * - Use cases: - Intensive processing: If you have tasks that involve intensive processing that can be divided into independent parts, you
 * can use `Schedulers.parallel()` to execute those parts in parallel, leveraging multiple available threads and speeding up the processing.
 * - Networking tasks: If you have network operations, such as API calls or HTTP requests, you can use `Schedulers.parallel()` to perform
 * multiple requests in parallel and make better use of the available network bandwidth.
 *
 * 2. `Schedulers.boundedElastic()`: This Scheduler provides a pool of elastic threads with a limited maximum capacity for task execution.
 * Here's an explanation and some use cases:
 *
 * - Explanation: `Schedulers.boundedElastic()` creates a pool of elastic threads with a limited maximum capacity. These threads are
 * suitable for non-blocking tasks and are designed to prevent thread overload in high load situations. If the number of tasks exceeds the
 * maximum capacity of available threads, additional tasks will be enqueued and executed efficiently.
 *
 * - Use cases: - I/O operations: If you have non-blocking input/output (I/O) operations, such as file reading/writing, database access, or
 * web service calls, `Schedulers.boundedElastic()` is a good choice. It allows performing these operations efficiently and ensures that
 * thread capacity won't be saturated under heavy workloads. - Asynchronous processing: If you need to execute asynchronous tasks that may
 * take time, such as image processing or running scheduled jobs, `Schedulers.boundedElastic()` provides a suitable environment with limited
 * capacity to manage the execution of these tasks without overloading system resources.
 *
 * Remember, the choice of the appropriate Scheduler depends on the type of task you're performing and the requirements for performance and
 * resources. These examples will help you better understand how and when to use `Schedulers.parallel()` and `Schedulers.boundedElastic()`
 * in Reactor.
 * <p>
 */
@Slf4j
class SchedulerTest {

  private final List<String> nameList = List.of("AAAA", "BBBB", "CCCC");

  private final List<String> nameList2 = List.of("DDDD", "EEEE", "FFFF");

  @Test
  void publishOn() {
    //when
    var value = Flux.fromIterable(nameList)
        .map(SchedulerTest::getString)
        .log();

    var value1 = Flux.fromIterable(nameList2)
        .map(SchedulerTest::getString)
        .log();

    //then
    StepVerifier.create(value.mergeWith(value1))
        .expectNextCount(6)
        .verifyComplete();
  }

  @Test
  void publishOnBoundedElastic() {
    //when
    var value = Flux.fromIterable(nameList)
        .publishOn(Schedulers.boundedElastic())
        .map(SchedulerTest::getString)
        .log();

    var value1 = Flux.fromIterable(nameList2)
        .publishOn(Schedulers.boundedElastic())
        .map(SchedulerTest::getString)
        .log();

    //then
    StepVerifier.create(value.mergeWith(value1))
        .expectNextCount(6)
        .verifyComplete();
  }

  @Test
  void subscribeOnnBoundedElastic() {
    //when
    var value = Flux.fromIterable(nameList)
        .subscribeOn(Schedulers.boundedElastic())
        .map(SchedulerTest::getString)
        .log();

    var value1 = Flux.fromIterable(nameList2)
        .map(SchedulerTest::getString)
        .subscribeOn(Schedulers.boundedElastic())
        .map(it -> {
          log.debug("New {}", it);
          return it;
        })
        .log();

    //then
    StepVerifier.create(value.mergeWith(value1))
        .expectNextCount(6)
        .verifyComplete();
  }

  @Test
  void publishOnParallel() {
    //when
    var value = Flux.fromIterable(nameList)
        .publishOn(Schedulers.parallel())
        .map(SchedulerTest::getString)
        .log();

    var value1 = Flux.fromIterable(nameList2)
        .publishOn(Schedulers.parallel())
        .map(SchedulerTest::getString)
        .log();

    //then
    StepVerifier.create(value.mergeWith(value1))
        .expectNextCount(6)
        .verifyComplete();
  }

  private static String getString(String it) {
    delay(1000);
    log.debug("Name is {}", it);
    return it;
  }

  @Test
  void subscribeParallel() {
    //when
    var value = Flux.fromIterable(nameList)
        .subscribeOn(Schedulers.parallel())
        .map(SchedulerTest::getString)
        .log();

    var value1 = Flux.fromIterable(nameList2)
        .map(SchedulerTest::getString)
        .subscribeOn(Schedulers.parallel())
        .map(SchedulerTest::getString)
        .log();

    //then
    StepVerifier.create(value.mergeWith(value1))
        .expectNextCount(6)
        .verifyComplete();
  }

  /**
   * In this example, we use `subscribeOn(Schedulers.parallel())` to perform number processing on a parallel thread, which means the numbers
   * will be processed simultaneously on multiple threads. Then, we use `publishOn(Schedulers.single())` to publish the results on a
   * separate thread, ensuring that the result emission occurs on a single thread.
   */
  @Test
  void example1() {
    //when
    Flux.range(1, 10)
        .subscribeOn(Schedulers.parallel()) // Realiza el procesamiento en un hilo paralelo
        .map(number -> {
          log.debug("Procesando número: " + number + ", Thread: " + Thread.currentThread().getName());
          return number * 2;
        })
        .publishOn(Schedulers.single()) // Publica los resultados en un hilo separado
        .map(result -> {
          log.debug("Resultado: " + result + ", Thread: " + Thread.currentThread().getName());
          return result;
        })
        .log()
        .blockLast();

  }

  /**
   * In this case, we use `subscribeOn(Schedulers.elastic())` to perform the data download on an elastic thread, which automatically adjusts
   * according to the workload. Then, we use `publishOn(Schedulers.single())` to publish the results on a separate thread, ensuring that the
   * result emission occurs on a single thread.
   */
  @Test
  void example2() {
    //when
    Mono.fromCallable(() -> {
          // Simulación de la descarga de datos desde una API
          delay(2000);
          return "Datos descargados";
        })
        .subscribeOn(Schedulers.parallel()) // Realiza la descarga en un hilo elástico
        .doOnSubscribe(subscription -> log.debug("Iniciando descarga en el hilo: " + Thread.currentThread().getName()))
        .publishOn(Schedulers.single()) // Publica los resultados en un hilo separado
        .map(result -> {
          log.debug("Datos recibidos: " + result + ", Thread: " + Thread.currentThread().getName());
          return result;
        })
        .log()
        .block();

  }
}
