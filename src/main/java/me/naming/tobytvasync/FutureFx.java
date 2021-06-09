package me.naming.tobytvasync;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import lombok.extern.log4j.Log4j2;

/**
 * @description : 비동기 작업 클래스를 임의로 만든 경우
 */
@Log4j2
public class FutureFx {

  private static Object CallbackFutureTask;

  interface SuccessCallback {
    void onSuccess(String result);
  }

  interface ExceptionCallback {
    void onError(Throwable t);
  }

  /** FutureTask를 상속받은 클래스가 객체가 되기 위해서는 Callable 인터페이스를 매개변수로 받아 부모 클래스에 넘겨줘야 한다. */
  public static class CallbackFutureTask extends FutureTask<String> {
    SuccessCallback sc;
    ExceptionCallback ec;

    public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
      super(callable);
      this.sc = Objects.requireNonNull(sc);
      this.ec = Objects.requireNonNull(ec);
    }

    @Override
    protected void done() {
      try {
        log.info("CallbackFutureTask done method working-1");
        sc.onSuccess(get());
        log.info("CallbackFutureTask done method working-2");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (ExecutionException e) {
        log.info("ExecutionException Error-1");
        ec.onError(e.getCause());
        log.info("ExecutionException Error-2");
      }
    }

    @Override
    protected void setException(Throwable t) {
      super.setException(t);
    }
  }

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    ExecutorService ex = Executors.newCachedThreadPool();

    // 아래 코드와 같이 FutureTask 오브젝트 형태로 만들 수 있다.
    //    Future<String> f = ex.submit(() -> {
    //      Thread.sleep(2000);
    //      log.info("Async Hello");
    //      return "Hello";
    //    });

    CallbackFutureTask f =
        new CallbackFutureTask(
            () -> {
              if(1==1) throw new RuntimeException("Async Exception");
              log.info("Before Thread Sleep");
              Thread.sleep(2000);
              log.info("Async Hello");
              return "Hello";
            },
            res -> {
              log.info("(Working) Main method-1");
              System.out.println(res);
              log.info("(Working) Main method working-2");
            },
            e -> {
              log.info("(Exception) Main method-1");
              System.out.println("Error : "+e.getMessage());
              log.info("(Exception) Main method-2");
            });
    
    ex.execute(f);
    ex.shutdown(); // 해당 코드를 작성하지 않으면 스레드가 계속해서 동작한다.

//    FutureTask<String> f =
//        new FutureTask<>(
//            () -> {
//              Thread.sleep(2000);
//              log.info("Async Hello");
//              return "Hello";
//            }) {
//          @Override
//          protected void done() {
//            try {
//              System.out.println(get());
//            } catch (InterruptedException e) {
//              e.printStackTrace();
//            } catch (ExecutionException e) {
//              e.printStackTrace();
//            }
//          }
//        };

    //    System.out.println(f.get());    //Future Get은 스레드가 블록킹된다. 스레드 2개를 만들어 놓고서는 더 빠른 시스템을 만들어 놓은게 아니다.
    //    log.info("Exit");

  }
}
