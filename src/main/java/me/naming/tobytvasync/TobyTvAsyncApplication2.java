package me.naming.tobytvasync;

import java.util.concurrent.Callable;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 1. Servlet 버전별 특징
 *  Servlet 3.0 : 비동기 서블릿
 *    - HTTP connection 은 이미 논블록킹 IO
 *    - 서블릿 요청 읽기, 응답 쓰기는 논블록킹
 *
 *  Servlet 3.1 : 논블록킹 IO
 *    - 논블록킹 서블릿 요청, 응답 처리
 *    - Callback
 *
 * 2. 스프링 스레드 동작 로직
 * 작업1    ServletThread_1 - req -> WorkThread -> res(html)
 * 작업2    ServletThread_2 - req ...
 * => 위와 같은 방식으로 작업이 수행된다. 이때 ServletThread가 Blocking IO로 작업된다면, 비즈니스 로직 처리 시간동안 서블릿 스레드가 대기 상태로 있어 리소스를 효율적으로 사용X
 *    만약 스레드풀이 100개라고 한다면 200개 요청이 왔을 경우 100개 스레드가 동시에 작업하고 100개가 대기 상태에 있음으로써 상당한 Latency가 발생한다.
 *
 *    그렇다면 Thread Pool을 무한점 늘린다면 어떨까???
 *    Thread Pool을 무한정 늘리게 된다면 그만큼 Memory 에 많은 스레드를 차지하게 되고 이런 경우 Out of Memory 현상이 발생될 수 있다.
 *
 *    ==> 그렇다면??? 어떻게 하는게 효율적일까?
 *        ServletThread 자체를 비동기적으로 실행함으로써 비지니스 로직에서 WorkerThread가 동작하는 동안 ServletThread를 pool에 되돌려주고
 *        다른 작업을 실행하게 되는 것이다. 즉, 비동기적으로 작업을 수행하는 것이 가장 리소스를 효율적으로 사용 할 수 있게된다.
 */
@EnableAsync // 해당 어노테이션을 통해 비동기적으로 @Async 메서드가 동작한다.
@SpringBootApplication
@Log4j2
public class TobyTvAsyncApplication2 {

  @RestController
  public static class MyController {

    @GetMapping("/sync")
    public String sync() throws InterruptedException {
      Thread.sleep(2000);
      return "hello";
    }

    @GetMapping("/async")
    public Callable<String> async() throws InterruptedException {
      log.info("** callable");
      return () ->{
        log.info("** async Callable");
        Thread.sleep(2000);
        return "hello";
      };
    }

    @GetMapping("/callable")
    public String callable() throws InterruptedException {
      log.info("async");
      Thread.sleep(2000);
      return "hello";
    }
  }
  public static void main(String[] args) {
    SpringApplication.run(TobyTvAsyncApplication2.class, args);
  }
}