package me.naming.tobytvasync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Tom on 2021/06/24
 * (7) CompletableFuture
 */
@Slf4j
public class ComFuture {

  /**
   * @description : CompletableFuture
   *  - 리스트의 모든 값이 완료될 때까지 기다릴지 아니면 하나의 값만 완료되길 기다릴지 선택할 수 있음.
   *  - cpu core 사이에 지연 실행이나 예외를 callable하게 처리할 수 있어서 명시적인 처리가 가능해짐.
   *  - 주요 메소드
   *    : runAsync 비동기적으로 실행.
   *    : thenRun 동일한 스레드에서 선행작업이 완료되고 실행. 즉 동기적으로 실행.
   *    : supplyAsync 결과 값을 리턴 할 수 있다
   *    : thenApply 선행 작업에서 전송해준 결과 값을 받아서 처리
   */
  public static void main(String[] args) throws InterruptedException {

    CompletableFuture
        .runAsync(() -> log.info("run async"))
        .thenRun(() -> log.info("then run1"))
        .thenRun(() -> log.info("then run2"));

    CompletableFuture
        .supplyAsync(() -> {
          log.info("supplyAsync");
          return 1;
        })
        .thenApply(s -> {
          log.info("thenApply : {}", s);
          return s + 1;
        })
        .thenAccept(s2 -> log.info("thenAccept : {}", s2));

    log.info("** exit");

    ForkJoinPool.commonPool().shutdown();
    ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
  }

}
