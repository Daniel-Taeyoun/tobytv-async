package me.naming.tobytvasync;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@SpringBootApplication
public class RemoteService {

  @RestController
  public static class MyController {
    @GetMapping("/service")
    public String service(String req) throws InterruptedException {
      System.out.println("Service111 : "+req);
      Thread.sleep(1000);
      return req + " /apiService_1";
    }

    @GetMapping("/service2")
    public String service2(String req) throws InterruptedException {
      System.out.println("Service222 : "+req);
      Thread.sleep(1000);
      return req + " /apiService_2";
    }

    @GetMapping("/service3")
    public String service3(String req) throws InterruptedException {
      Thread.sleep(1000);
      return req + " /apiService_3";
    }
  }

  public static void main(String[] args){
    System.setProperty("server.port", "8081");
    System.setProperty("server.tomcat.threads.max", "1000");
    SpringApplication.run(RemoteService.class, args);
  }

}
