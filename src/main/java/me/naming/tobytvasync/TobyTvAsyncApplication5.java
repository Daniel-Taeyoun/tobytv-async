package me.naming.tobytvasync;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * (10) Flux 특징과 활용방법 Flux와 Mono의 차이점은?
 */
@SpringBootApplication
public class TobyTvAsyncApplication5 {

  @RestController
  public static class WebFluxTestController2 {

    @GetMapping("/event/{id}")
    public Mono<Event> getEvent(@PathVariable long id) {
      return Mono.just(new Event(id, "Event1"));
    }

//    @GetMapping("/events")
//    public Flux<Event> getEvents1() {
//      return Flux.just(new Event(1, "Event1"), new Event(2, "Event2"));
//    }

    @GetMapping("/events")
    public Flux<Event> getEvents2() {
      List<Event> list = Arrays.asList(new Event(1L, "Event1"), new Event(2L, "Event2"));
      return Flux.fromIterable(list);
    }

    @Data
    @AllArgsConstructor
    public static class Event {

      private long id;
      private String name;
    }

  }

  public static void main(String[] args) {
    System.setProperty("server.port", "8090");
    SpringApplication.run(TobyTvAsyncApplication5.class);
  }
}
