package me.naming.tobytvasync;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class LoadTest {

  static AtomicInteger counter = new AtomicInteger(0);

  public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
    ExecutorService es = Executors.newFixedThreadPool(100);

    RestTemplate rt = new RestTemplate();
    String webClientUrl = "http://localhost:8080/webclient?idx={idx}";
    String url = "http://localhost:8080/rest/callback/refactoring?idx={idx}";
//    String url = "http://localhost:8080/rest/callback?idx={idx}";

    CyclicBarrier barrier = new CyclicBarrier(101);

    for(int i=0; i<100; i++) {
      es.submit(() -> {
        int idx = counter.addAndGet(1);

        //CyclicBarrier 클래스를 통해 100개 쓰레드가 올때까지 대기했다가 동시에 실행합니다.
        barrier.await();

        log.info("** Lambda Thread {}", idx);

        StopWatch sw = new StopWatch();
        sw.start();

        String res = rt.getForObject(webClientUrl, String.class, idx);

        sw.stop();
        log.info("** Finish: {} / {} / {}", idx, sw.getTotalTimeSeconds(), res);
        return null;
      });
    }

    barrier.await();
    StopWatch main = new StopWatch();
    main.start();

    es.shutdown();
    es.awaitTermination(100, TimeUnit.SECONDS);
    main.stop();

    log.info("** Total: {}", main.getTotalTimeSeconds());
  }
}
