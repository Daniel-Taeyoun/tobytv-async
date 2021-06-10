# TobyTvAsync
유튜브 채널 "토비의 봄"에서 스프링 리액티브 프로그래밍 실습 코드 작성

## 설명내용
### @Async 
- @Async 어노테이션으로 비동기 작업을 실행한다면 SimpleAsyncTaskExecutor를 통해 매번 새로운 스레드를 생성하게 됩니다.
- 예를 들어, 100개 비동기 작업 요청이 들어오면 스프링에서 Thread 100개를 모두 생성하고 실행하게 됩니다.  
  이런 경우 Thread 자체에 캐시도 설정되어 있지 않아 매번 새롭게 스레드를 생성&소멸하게 됨으로써 CPU에 부담이 되고 성능저하로 이어질 수 있습니다.

### Servlet 버전별 특징
우선 서블릿이란?  
서블릿이란 기본적으로 웹 어플리케이션에서 네트워크 요청에 대한 응답을 해주는 방식이라고 볼 수 있습니다. (응답 해주는 방식은 웹페이지)
- 3.0은 비동기 서블릿. 3.1은 논블록킹 IO의 특징을 갖고 있습니다.
- ?) 비동기와 논블록킹. 동기와 블록킹. 이것은 무엇일까? . 그리고 비동기면 무조건 논블록킹인가? => 참고에 '동기 비동기 블록킹 ~'내용 읽어볼 것  

### 비동기 RestTemplate과 비동기 MVC 결합  
AsyncRestTemplate을 이야기하기 전에 우선 Http 콜할때 RestTemplate을 사용했을 때 어떤 한계점이 있을까?
토비님께서 극단적인 테스트 환경을 셋팅해줬는데 Tomcat Thread는 1개로 설정하고 하나의 API에서 처리 시간이 2초 걸린다고 했을 경우
동시에 100명의 Client가 요청을 보낸다면 어떻게될지 테스트 해보았다.

테스트 결과를 보면 놀랍다.

- RestTemplate은 기본적으로 블록킹 방식이기 때문에 Client의 동시 요청이 발생했을 경우 100건의 요청이 발생한 경우 각 건당 2초 이상 소요시간이 처리되고,
따라서 100개 요청 x 2초 => 200초 시간이 걸리게 된다.

- 반면 AsyncRestTemplate(Spring 5.0에서는 Deprecated. WebClient를 추천)을 사용할 경우 논블록킹 처리방식임으로
한개 Thread로 100개 요청을 2초에 처리해낼 수가 있다.(놀라워놀라워 ~.~)

## 아하!! 느낀점
- JMC, Visual VM을 활용해 현재 작성된 코드가 메모리 사이즈를 얼마나 차지하는지, 톰캣 쓰레드는 몇개나 생성되었는지. Live Thread의 peak는 어떻게 되는지 본다면 매우 흥미롭다.

## 기타
- `server.tomcat.threads.max` tomcat의 최대 스레드 갯수를 지정해줄 수 있다.(Default는 200개. ServerProperties에서 확인 가능)
- Tomcat Thread 갯수를 제한하고 100개 요청을 한번에 보내면 서블릿 스레드(http-nio ~~)는 10개로 제한되지만 내부적으로 Worker Thread가 계속해서 생겨난다.  
  (토비 영상에서는 워커 스레드가 무한정 생성되는 것 같지만, 실질적으로 테스트 해본결과 Worker Thread는 조건에 따라 갯수가 한정된다.)

**참고**  
Servlet 관련 내용 : https://mangkyu.tistory.com/14, https://stackoverflow.com/questions/7213541/what-is-java-servlet  
동기 비동기와 블록킹 논블록킹 개념 : https://deveric.tistory.com/99
