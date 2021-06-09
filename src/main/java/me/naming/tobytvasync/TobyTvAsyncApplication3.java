package me.naming.tobytvasync;

import io.netty.channel.nio.NioEventLoopGroup;
import java.net.NetPermission;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


/**
 * (5) 비동기 RestTemplate과 비동기 MVC의 결합
 *
 * 예를 들어,
 * FrontEnd -> Profile Backend
 *          -> Payment Backend
 *          -> Search Backend
 * 프런트 엔드에서 위의 3개 백엔드 각각에 API를 쏘고 응답 값을 받아온다고 가정해봅니다.
 * 이런 경우 동기적으로 동작하면서 Profile -> Payment -> Search API를 순차적으로 쏘게되고
 * 각각 1초에 대기 시간이 걸린다고 할 경우 3초 이상의 대기 시간이 발생하게 됩니다.
 *
 * 그렇다면 문제점은?
 *  1) Thread의 Context Switching이 발생함으로써 CPU 자원을 소모하게되며
 *  2) Thread가 대기 상태에 들어가 있는 동안 CPU가 아무런 동작을 하지 않을 수 있게 됩니다. 즉, CPU 자원 낭비
 *  3) 또한, 대기 시간이 3초 이상 발생함으로써 응답 Latency가 급격히 올라갈 수 있으며,
 *  4) 만약, 해당 API로 대량 요청이 들어왔을 경우 <b Thread Pool은 고갈되서 요청이 다 대기하고, CPU는 놀고있고, Client는 응답 값을 못받고 있는>
 *     아이러니한 상황이 만들어질 수 있습니다.
 *
 * 해결 방법은?
 *  1) Thread Pool을 더 많이 생성하면 되지 않을까요?
 *    => 한꺼번에 많은 요청이 오게 된다면 CPU 자원은 놀고있고 Thread Pool은 고갈되서 응답을 못하는 상황이 발생 할 수 있습니다.
 *    => 또한, Thread를 메모리에 대기시킴으로써 메모리 자원을 소비하게 되고, 더 빠른 OOM(Out of Memory) 현상이 발생 할 수 있습니다.
 *
 * Q) Netty가 기본적으로 콜할때 Thread를 몇개 만들까요???
 *  => 프로세스 갯수 * 2개가 생성됨
 */
@SpringBootApplication
@Log4j2
public class TobyTvAsyncApplication3 {

  @RestController
  public static class MyController{
//    RestTemplate rt = new RestTemplate();
    AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));
    WebClient client = WebClient.create();

    @GetMapping("/rest")
    public ListenableFuture<ResponseEntity<String>> rest(int idx) {
      // 또 다른 서버(RemoteService)한테 요청 후 결과 값을 받아오는 방식
//      String result = rt.getForObject("http://localhost:8081/service?req={req}"
//          , String.class
//          ,"hello +"+idx);

      return rt.getForEntity("http://localhost:8081/service?req={req}" , String.class ,"hello +"+idx);
    }

  }

  public static void main(String[] args) {
    SpringApplication.run(TobyTvAsyncApplication3.class, args);
  }
}