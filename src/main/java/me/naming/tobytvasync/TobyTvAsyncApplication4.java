package me.naming.tobytvasync;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * (8) WebFlux
 * AsyncRestTemplate -> WebClient 변경
 */
@SpringBootApplication
@Log4j2
@RequiredArgsConstructor
@EnableAsync
public class TobyTvAsyncApplication4 {

  @RestController
  @RequiredArgsConstructor
  public static class WebClientTestController {

    static final String URL1 = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";
    static final String URL3 = "http://localhost:8081/service3?req={req}";

    final MyService myService;

    WebClient client = WebClient.create();

    /**
     * Spring 5.0 WebFlux를 사용할 경우 Mono 타입으로 리턴해야됩니다.
     * 선언만(client.get().uri("www.daum.net",idx).exchange();) 해두면 동작하지 않습니다.
     * bodyToMono 메서드를 작성해야 동작합니다.
     *
     * map이란 안에 원소 타입을 변경하는 방식. 예를 들어서 아래 코드. ClientReesponse -> String 형태로 변경
     * flatmap이란 이중으로 감싸주던 것을 납작하게 변경해주는 것. res.map(~)으로 했을 경우 리턴 타입은 Mono<Mono<String>> 타입인데, flatmap 으로 납작하게 수정
     */
    @GetMapping("/rest")
    public Mono<String> rest(int idx) {
      Mono<ClientResponse> res = client.get().uri(URL1,idx).exchange();
      Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
      return Mono.just("Hello");
    }

//    @GetMapping("/webClient")
//    public Mono<String> testClient(int idx) {
////      return client.get().uri(URL1, idx)
////          .exchangeToMono(b -> b.bodyToMono(String.class));
//      return client.get().uri(URL1, idx).exchange()
//          .flatMap(c -> c.bodyToMono(String.class))
//          .flatMap(res1 -> client.get().uri(URL2, res1).exchange())
//          .flatMap(rsp -> rsp.bodyToMono(String.class));
//    }

    @GetMapping("/webClient")
    public Mono<String> webClient(int idx) {
      return client.get().uri(URL1, idx)
          .exchangeToMono(c -> c.bodyToMono(String.class))
          .flatMap(rsp1 -> client.get().uri(URL2, rsp1).exchangeToMono(c -> c.bodyToMono(String.class)))
          .flatMap(rsp2 -> client.get().uri(URL3, rsp2).exchangeToMono(c -> c.bodyToMono(String.class)))
          .flatMap(rsp3 -> Mono.fromCompletionStage(myService.work(rsp3)))
          .doOnError(e -> e.getMessage());
    }
  }

  public static void main(String[] args) {
    System.setProperty("server.port", "8080");
    System.setProperty("reactor.ipc.netty.workerCount", "1");
    System.setProperty("reactor.ipc.netty.pool.maxConnections", "20000");
    SpringApplication.run(TobyTvAsyncApplication4.class);
  }

  @Service
  public static class MyService {
    @Async
    public CompletableFuture<String> work(String req) {
      return CompletableFuture.completedFuture(req + "/asyncwork");
    }
  }
}
