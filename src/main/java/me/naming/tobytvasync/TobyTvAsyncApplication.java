//package me.naming.tobytvasync;
//
//import lombok.extern.log4j.Log4j2;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.AsyncResult;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.stereotype.Component;
//import org.springframework.util.concurrent.ListenableFuture;
//
//@EnableAsync // 해당 어노테이션을 통해 비동기적으로 @Async 메서드가 동작한다.
//@SpringBootApplication
//@Log4j2
//public class TobyTvAsyncApplication {
//
//  final MyService myService;
//
//  public TobyTvAsyncApplication(MyService myService) {
//    this.myService = myService;
//  }
//
//  @Component
//  public static class MyService {
//
////    @Async // 해당 메소드를 비동기적으로 동작시킨다는 어노테이션
////    public Future<String> hello() throws InterruptedException {
////      log.info("MyService hello()-1");
////      Thread.sleep(2000);
////      log.info("MyService hello()-2");
////      return new AsyncResult<>("Hello");
////    }
//
//    /**
//     * - ListenableFuture는 스프링에서 제공해주는 클래스로써 Future 결과 값을 실행 후 동작하는 방식
//     * - @Async 어노테이션은 비동기적으로 실행될 때마다 SimpleAsyncTaskExecutor를 통해 매번 새로운 스레드를 생성한다.
//     *    예를 들어, 100개에 비동기 작업 요청이 들어오면 스레드 100개를 모두 생성하고 실행한다.
//     *    스레드 자체에 캐시도 설정되어 있지 않아 매번 새롭게 스레드를 생성하여 비효율적으로 동작한다. 많은 CPU와 Memory 작업을 먹습니다.
//     */
//    @Async
//    public ListenableFuture<String> hello() throws InterruptedException {
//      log.info("*** MyService hello()-1");
//      Thread.sleep(2000);
//      log.info("*** MyService hello()-2");
//      return new AsyncResult<>("Async Method Finish");
//    }
//  }
//
//  /**
//   * poolSize 10개 생성되고, 모든 스레드가 작업중인 경우. 이때 해당 작업들을 200개까지(QueueCapacity) 대기시켜놓다가
//   * 200개를 초과해서 작업이 발생한 경우 MaxPoolSize 100개가 생성된다
//   */
//  @Bean
//  ThreadPoolTaskExecutor tp () {
//    ThreadPoolTaskExecutor tpe = new ThreadPoolTaskExecutor();
//
//    // runtime 때 실행된다. 즉, 비동기 작업 요청이 들어왔을 때 10개까지 생성해준다. 컴파일되고 run될때 실행x. 비동기 작업 요청이 들어왔을 때 생성됨
//    tpe.setCorePoolSize(10);
//    tpe.setMaxPoolSize(100);
//    tpe.setQueueCapacity(200);
//    tpe.setThreadNamePrefix("** Tom Thread ");
//    tpe.initialize();
//    return tpe;
//  }
//
//
//  public static void main(String[] args) {
////    SpringApplication.run(TobyTvAsyncApplication.class, args);
//    try(ConfigurableApplicationContext c = SpringApplication.run(TobyTvAsyncApplication.class, args)) {
//
//    }
//  }
//
//  @Bean
//  ApplicationRunner run() {
//    return args -> {
//      log.info("*** ApplicationRunner run()");
//      //      Future<String> fResult = myService.hello();
//      //      log.info("Exit : {}", fResult.isDone());
//      //      log.info("Result : {}", fResult.get());
//
//      ListenableFuture<String> fResult = myService.hello();
//      fResult.addCallback(s -> System.out.println(s), e -> System.out.println(e.getMessage()));
//      log.info("*** Exit");
//    };
//  }
//}