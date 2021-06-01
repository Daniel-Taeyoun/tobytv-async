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
- ?) 비동기와 논블록킹. 동기와 블록킹. 이것은 무엇일까? . 그리고 비동기면 무조건 논블록킹인가?

## 기타
- `server.tomcat.threads.max` tomcat의 최대 스레드 갯수를 지정해줄 수 있다.(Default는 200개. ServerProperties에서 확인 가능)
- Tomcat Thread 갯수를 제한하고 100개 요청을 한번에 보내면 서블릿 스레드(http-nio ~~)는 10개로 제한되지만 내부적으로 Worker Thread가 계속해서 생겨난다.  
  (토비 영상에서는 워커 스레드가 무한정 생성되는 것 같지만, 실질적으로 테스트 해본결과 Worker Thread는 조건에 따라 갯수가 한정된다.)

**참고**  
Servlet 관련 내용 : https://mangkyu.tistory.com/14, https://stackoverflow.com/questions/7213541/what-is-java-servlet  
동기 비동기와 블록킹 논블록킹 개념 : https://deveric.tistory.com/99
