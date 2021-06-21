package me.naming.tobytvasync;

import io.netty.channel.nio.NioEventLoopGroup;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * (5) 비동기 RestTemplate과 비동기 MVC의 결합
 *
 * <p>예를 들어, FrontEnd -> Profile Backend -> Payment Backend -> Search Backend 프런트 엔드에서 위의 3개 백엔드 각각에
 * API를 쏘고 응답 값을 받아온다고 가정해봅니다. 이런 경우 동기적으로 동작하면서 Profile -> Payment -> Search API를 순차적으로 쏘게되고 각각 1초에
 * 대기 시간이 걸린다고 할 경우 3초 이상의 대기 시간이 발생하게 됩니다.
 *
 * <p>그렇다면 문제점은? 1) Thread의 Context Switching이 발생함으로써 CPU 자원을 소모하게되며 2) Thread가 대기 상태에 들어가 있는 동안
 * CPU가 아무런 동작을 하지 않을 수 있게 됩니다. 즉, CPU 자원 낭비 3) 또한, 대기 시간이 3초 이상 발생함으로써 응답 Latency가 급격히 올라갈 수 있으며,
 * 4) 만약, 해당 API로 대량 요청이 들어왔을 경우 <b Thread Pool은 고갈되서 요청이 다 대기하고, CPU는 놀고있고, Client는 응답 값을 못받고 있는>
 * 아이러니한 상황이 만들어질 수 있습니다.
 *
 * <p>해결 방법은? 1) Thread Pool을 더 많이 생성하면 되지 않을까요? => 한꺼번에 많은 요청이 오게 된다면 CPU 자원은 놀고있고 Thread Pool은
 * 고갈되서 응답을 못하는 상황이 발생 할 수 있습니다. => 또한, Thread를 메모리에 대기시킴으로써 메모리 자원을 소비하게 되고, 더 빠른 OOM(Out of
 * Memory) 현상이 발생 할 수 있습니다.
 *
 * <p>Q) Netty가 기본적으로 콜할때 Thread를 몇개 만들까요??? => 프로세스 갯수 * 2개가 생성됨
 */
@SpringBootApplication
@Log4j2
@EnableAsync
public class TobyTvAsyncApplication3 {

  @RestController
  @RequiredArgsConstructor
  public static class MyController {
    RestTemplate restTemplate = new RestTemplate();
    AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
    WebClient client = WebClient.create();

    static final String URL1 = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";
    final MyService myService;

    @GetMapping("/rest/restTemplate")
    public String restTemplateCall(int idx) {
      // 또 다른 서버(RemoteService)한테 요청 후 결과 값을 받아오는 방식
      String result = restTemplate.getForObject("http://localhost:8081/service?req={req}"
          , String.class
          , "hello +" + idx);
      return result;
    }

    @GetMapping("/rest/async")
    public ListenableFuture<ResponseEntity<String>> rest(int idx) {
      return asyncRestTemplate.getForEntity(URL1, String.class, "hello +" + idx);
    }

    @GetMapping("/rest/deferred")
    public DeferredResult<String> deferredResult(int idx) {
      DeferredResult<String> dr = new DeferredResult<>();

      ListenableFuture<ResponseEntity<String>> f1 = asyncRestTemplate.getForEntity(URL1, String.class, "hello " + idx);

      f1.addCallback(
          s -> {
            dr.setResult(s.getBody() + "/work");
          },
          e -> {
            /**
             * 비동기 상황에서 MessageException을 무분별하게 Throw하면 정확하게 스프링 Mvc 지시한 곳에서 정확하게 받을 것이라는 보장이 없다.
             * 따라서 DeferredResult에 해당 에러 메세지를 저장해서 리턴
             */
            dr.setErrorResult(e.getMessage());
          });
      return dr;
    }

    @GetMapping("/rest/callback")
    public DeferredResult<String> callbackResult(int idx) {
      DeferredResult<String> dr = new DeferredResult<>();

      ListenableFuture<ResponseEntity<String>> firstListenableFuture = asyncRestTemplate.getForEntity(URL1, String.class, "hello " + idx);
      /** 주의사항!!! - DeferredResult 사용 시 아래 (a) 메세지와 같은 현상이 발생 할 수 있다. */
      firstListenableFuture.addCallback(
          s -> {
            ListenableFuture<ResponseEntity<String>> sndListenableFuture = asyncRestTemplate.getForEntity(URL2, String.class, "hello " + idx);
            sndListenableFuture.addCallback(
                s2 -> {
//                  dr.setResult(s2.getBody());
                  ListenableFuture<String> thirdListenableFuture = myService.work(s2.getBody());
                  thirdListenableFuture.addCallback(s3 -> {
                    dr.setResult(s3);
                  }, e3 -> {
                    dr.setErrorResult(e3);
                  });
                },
                e2 -> {
                  dr.setErrorResult(e2.getMessage());
                });
            /*dr.setResult(s.getBody());  -- a. DeferredResult를 두번째 API(/service2)에서 설정하지 않고
            첫번째 API(/service)에서 호출하는 경우 두번째 결과값을 대기하지 않고 바로 리턴*/
          },
          e -> {
            dr.setErrorResult(e.getMessage());
          });
      return dr;
    }

    /**
     * (6) AsyncRestTemplate 콜백 헬과 중복 작업 문제
     * @param idx
     * @return
     */
    @GetMapping("/rest/callback/refactoring")
    public DeferredResult<String> callbackResultRefactoring(int idx) {
      DeferredResult<String> dr = new DeferredResult<>();
      Completion
          .from(asyncRestTemplate.getForEntity(URL1, String.class, "hello " + idx))
          .andApply(s -> asyncRestTemplate.getForEntity(URL2, String.class, s.getBody()))
          .andApply(s -> myService.work(s.getBody()))
          .andError(e -> dr.setErrorResult(e.toString()))
          .andAccept(s -> dr.setResult(s));

      return dr;
    }

    public static class AcceptCompletion<S> extends Completion<S, Void>{
      public Consumer<S> con;
      public AcceptCompletion(Consumer<S> con){
        this.con = con;
      }

      @Override
      void run(S value) {
        con.accept(value);
      }
    }

    public static class ErrorCompletion<T> extends Completion<T, T> {
      public Consumer<Throwable> econ;
      public ErrorCompletion(Consumer<Throwable> econ){
        this.econ = econ;
      }

      @Override
      void error(Throwable e) {
        econ.accept(e);
      }

      @Override
      void run(T value) {
        if(next != null) next.run(value);
      }
    }

    public static class ApplyCompletion<S, T> extends Completion<S, T> {
      public Function<S, ListenableFuture<T>> fn;
      public ApplyCompletion<Function<S, ListenableFuture<T>> fn> {
        this.fn = fn;
      }

      @Override
      void run(S value) {
        ListenableFuture<T> lf = fn.apply(value);
        lf.addCallback(s -> complete(s), e -> error(e));
      }
    }



    public static class Completion<S, T> {
      Completion next;

      public void andAccept(Consumer<T> con) {
        Completion<T, Void> c = new AcceptCompletion<>(con);
        this.next = c;
      }
      public Completion<T, T> andError(Consumer<Throwable> econ){
        Completion<T, T> c = new ErrorCompletion<>(econ);
        this.next = c;
      }

      public <V> Completion<T,V> andApply(Function<T, ListenableFuture<V>> fn) {
        Completion<T, V> c = new ApplyCompletion<>(fn);
        this.next = c;
        return c;
      }

      public static <S, T> Completion<S, T> from(ListenableFuture<T> lf) {
        Completion<S, T> c = new Completion<S, T>();
        lf.addCallback(s -> {
          c.complete(s);
        }, e -> {
          c.error(e);
        });
        return c;
      }

      void error(Throwable e) {
      }

      void complete(T s) {
        if(next != null) next.run(s);
      }

      void run(S value) {
      }
    }

    @Service
    public static class MyService {
      @Async
      public ListenableFuture<String> work(String req) {
        return new AsyncResult<>(req + "/asyncwork");
      }
    }

    @Bean
    public ThreadPoolTaskExecutor myThreadPool() {
      ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
      te.setCorePoolSize(1);
      te.setMaxPoolSize(1);
      te.initialize();
      return te;
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(TobyTvAsyncApplication3.class, args);
  }
}
