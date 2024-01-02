# Spring security와 Jwt를 활용한 회원 관리 애플리케이션

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [프로젝트 개발도구](#프로젝트-개발도구)
- [API 설계](#api-설계-api-설계-상세-보기postman-document)
- [Access token을 사용한 사용자 인증 과정](#access-token을-사용한-사용자-인증-과정)
- [Refresh token을 사용한 Access token 재발행 과정](#refresh-token을-사용한-access-token-재발행-과정)
- [Access token 및 Refresh token의 활용](#access-token-및-refresh-token의-활용)
- [프로젝트 후기](#프로젝트-후기)

---

### 프로젝트 소개

&nbsp;&nbsp; Java와 Spring을 바탕으로 웹 개발에 대해 학습하며, 그동안 학습한 내용을 녹여내어 개발한 회원 관리 애플리케이션입니다.
회원 관리 기능을 선택하여 개발한 이유는, IT 교육기관에서 진행한 파이널 프로젝트에서 회원 관리기능을 주로 담당하였으나, 다소 단순한 구조로 개발을 진행했던 부분에 아쉬움을 느껴, 보완해 보고자 이러한 주제를 선택했습니다.

---

### 프로젝트 개발도구

- SpringBoot : 3.2.0
- Spring security : 6.2.0
- jjwt : 0.12.3
- JDK : 19
- Spring data JPA : 3.2
- MySQL

- POSTMAN
- IntelliJ

---

### API 설계 ([API 설계 상세 보기(Postman document)](https://documenter.getpostman.com/view/24944136/2s9Ykt4ygu))

| **Method** | **URI**                          | **Header**                              | **Body**                           | **수행 기능**                          |
| ---------- | -------------------------------- |-----------------------------------------| ---------------------------------- | -------------------------------------- |
| POST       | /auth                            |                                         |                                    | 로그인 요청                            |
| PUT        | /auth                            |                                         | {"refreshToken":"${refreshToken}"} | Access token 재발행 요청               |
| DELETE     | /auth                            | Authorization: Bearer "${refreshToken}" |                                    | 로그아웃 요청                          |
| GET        | /members?page={page}&size={size} | Authorization: Bearer "${accessToken}"  |                                    | 회원 목록 조회 요청                    |
| GET        | /members/{id}                    | Authorization: Bearer "${accessToken}"  |                                    | id에 해당하는 단일 회원 정보 조회 요청 |
| POST       | /members                         | Authorization: Bearer "${accessToken}"  | 회원 가입 정보(JSON)               | 회원 가입 요청                         |
| PUT        | /members/{id}                    | Authorization: Bearer "${accessToken}"  | 수정할 회원 정보(JSON)             | id에 해당하는 회원 정보 수정 요청      |
| DELETE     | /members/{id}                    | Authorization: Bearer "${accessToken}"  |                                    | id에 해당하는 회원정보 삭제 요청       |

---

### 로그인 과정

![](/readme_img/login.svg)

- 사용자로부터 로그인 요청을 받습니다.
- Spring Security의 Filter를 거치며 AnonymousAuthenticationToken을 생성하여 SecurityContextHolder에 저장합니다. AnonymousAuthenticationToken는 인증되지 않은 사용자를 의미합니다.
- DispatcherServlet는 요청을 확인하고, 요청을 처리할 Controller를 찾아 요청을 전달합니다.
- 로그인 기능을 담당하는 AuthenticationController에서 AuthenticationManager를 호출하여 로그인 입력정보를 바탕으로 인증을 시도합니다.
- AuthenticationManager는 UsernamePasswordAuthenticationToken을 인증할 수 있는 AuthenticationProvider를 찾아 해당 인증 정보를 전달합니다.
- UsernamePasswordAuthenticationToken를 인증할 수 있는 DaoAuthenticationProvider는 UsernamePasswordAuthenticationToken에 포함된 인증 정보를 UserDetailsService을 통해 DB에 저장된 인증 정보와 일치하는지 확인하고 결과를 반환합니다.

- 인증에 실패할 경우, 컨트롤러까지 예외가 전달되어 해당 예외의 정보가 ControllerExceptionHandler로 전달됩니다.
- ControllerExceptionHandler는 예외의 정보가 담긴 ResponseEntity를 생성하고, DispatcherServlet으로 전달합니다.

- AuthenticationManager에 의해 인증이 완료된 UsernamePasswordAuthenticationToken은 AuthenticationService로 전달됩니다.
- AuthenticationService는 인증 객체에서 추출한 사용자 정보를 JwtUtils로 전달하여 Access token과 Refresh token을 생성시킵니다.
- JwtUtils에서 생성한 토큰을 포함한 로그인 응답용 객체를 생성하여 AuthenticationController로 전달합니다.
- AuthenticationController는 로그인 응답 객체를 포함한 ResponseEntity를 생성하여 DispatcherServlet으로 전달합니다.
- DispatcherServlet은 Http Response를 클라이언트로 전달합니다.
- 이후 클라이언트는 로그인 응답객체에 포함된 Access token과 Refresh token을 저장하고, 필요 시 활용할 수 있습니다.

---

### Access token을 사용한 사용자 인증 과정

![](/readme_img/access_token.svg)

- 사용자로부터 회원 삭제 요청을 받습니다.
- 인증이 필요한 요청인 경우, JwtAuthenticationFilter는 Access token 포함한 JwtAuthenticationToken을 생성하여 JwtAuthenticationProvider에게 전달하며 인증을 요청합니다.
- JwtAuthenticationProvider는 JwtUtils를 통해 Access token의 유효성을 검사하고, 토큰에서 추출한 사용자 정보를 JwtAuthenticationToken에 담아 반환합니다.

- 인증 과정에서 예외가 발생한 경우, JwtAuthenticationiFilter로 예외가 전달되어, CustomAuthenticationEntryPoint가 호출됩니다.
- CustomAuthenticationEntryPoint는 응답 헤더에 'JwtException' 필드를 추가하고, 예외 정보가 담긴 객체를 포함한 ResponseEntity를 생성합니다.
- 생성된 ResponseEntity는 DispatcherServlet으로 전달합니다.

- JwtAuthenticationFilter는 인증에 성공한 JwtAuthenticationToken을 SecurityContextHolder에 저장하고, 남아있는 Filter가 실행되도록 합니다.
- DispatcherServlet에서 요청을 확인하고, 요청을 처리할 Controller를 찾습니다.
- 회원 삭제 기능을 담당하는 MemberController가 호출될 때, TokenToMemberInfoArgumentResolver는 SecurityContextHolder에 저장된 JwtAuthenticationToken에서 사용자 정보를 추출하여, 로그인한 사용자의 정보가 담긴 LoginMemberInfo 객체를 생성합니다.
- MemberController는 TokenToMemberInfoArgumentResolver에 의해 생성된 로그인한 사용자 정보를 전달받습니다.
- MemberController에서 로그인 한 사용자 정보를 기준으로, 삭제 대상 회원을 삭제할 권한이 있는지 판단하고, 판단 결과에 따른 처리를 합니다.
- 회원 삭제 요청에 대한 처리가 완료되면, MemberController는 ResponseEntity를 생성하여 DispatcherServlet으로 전달합니다.
- DispatcherServlet은 Http Response를 클라이언트로 전달합니다.
---

### Refresh token을 사용한 Access token 재발행 과정

![](/readme_img/refresh_token.svg)

- 사용자로부터 Access token 재발행 요청을 받습니다.
- Spring Security의 Filter를 거치며 AnonymousAuthenticationToken을 생성하여 SecurityContextHolder에 저장합니다. AnonymousAuthenticationToken는 인증되지 않은 사용자를 의미합니다.
- DispatcherServlet는 요청을 확인하고, HandlerMapping에 의해 요청을 처리할 Controller를 찾습니다.
- Access token 재발행 기능을 담당하는 AuthenticationController는 요청 바디에 포함된 Refresh token을 AuthenticationService를 전달하여 Access token의 재발행을 요청을 수행합니다.
- AuthenticationService는 기존에 발행한 Refresh token인지 DB를 조회하여 확인합니다.
- 발행했던 토큰임이 확인되면, JwtUtils에 토큰을 전달하여 새로운 Access token을 발급받습니다.
- 이때, Access token의 재발행은 Refresh token을 parsing하여, Refresh token의 Claims를 기반으로 재발행됩니다.

- Access token 재발행에 실패한 경우, 컨트롤러까지 예외가 전달되어 해당 예외의 정보가 ControllerExceptionHandler로 전달됩니다.
- ControllerExceptionHandler는 응답 헤더에 'JwtException' 필드를 추가하고, 예외 정보가 담긴 객체를 포함한 ResponseEntity를 생성합니다.
- 생성된 ResponseEntity는 DispatcherServlet으로 전달합니다.

- JwtUtils는 재발행된 Access token을 AuthenticationService로 전달하며, AuthenticationService는 재발행된 Access token과 기존 Refresh token 등을 담은 응답 객체를 생성하여 AuthencationController로 전달합니다.
- AuthenticationController는 응답 객체를 포함한 ResponseEntity를 생성하여 DispatcherServlet으로 전달합니다.
- DispatcherServlet은 Http Response를 클라이언트로 전달합니다.
- 이후 클라이언트는 응답 객체에 포함된 Access token과 Refresh token을 저장하고, 필요 시 활용할 수 있습니다.

---

### Access token 및 Refresh token의 활용

![](/readme_img/token_cycle.svg)

1.  클라이언트는 이전 로그인 시 발행됐던 Access token을 매 요청마다 헤더에 포함하여 요청을 보냅니다.
2.  서버는 Access token의 유효성에 문제가 발생한 경우, 응답 헤더에 JwtException 필드를 추가하고, 발생한 예외의 종류를 담아 클라이언트로 전달합니다.  
    (위 예시에서는 Access token의 만료를 가정했습니다.)
3.  클라이언는 응답 상태코드 및 헤더의 JwtException 필드를 확인하여 Access token의 만료를 확인합니다.  
    Access token을 재발급 받기 위해, 요청 바디에 Refresh token을 포함하여 요청을 보냅니다.
4.  서버는 Refresh token의 유효성에 문제가 발생한 경우, 응답 헤더에 JwtException 필드를 추가하고, 발생한 예외의 종류를 담아 클라이언트로 전달합니다.  
    (위 예시에서는 Refresh token의 만료를 가정했습니다.)
5.  클라이언는 응답 상태코드 및 헤더의 JwtException 필드를 확인하여 Refresh token의 만료를 확인합니다.  
    사용가능한 모든 토큰이 만료됐으므로, 새로운 토큰을 발급받기 위해 로그인을 다시 요청합니다.
6.  서버는 로그인 정보를 확인하여 새로운 Access token과 Refresh token을 발행하여 클라이언트로 전달합니다.  
    이후, 클라이언트는 새로 발급받은 Access token을 활용하여 인증이 필요한 요청에 접근할 수 있습니다.

    ***

### 프로젝트 후기

&nbsp;&nbsp;특정한 비즈니스 상황을 가정하고 개발한 것이 아닌, 인증 및 회원 데이터에 대한 CRUD 작업을 수행하는 비교적 단순한 애플리케이션입니다. 하지만, IT 교육기관에서 개발했던 프로젝트보다 작게나마 한 걸음 더 나아갔다는 생각에 뿌듯함을 느낄 수 있었던 프로젝트였습니다.
작은 애플리케이션인 만큼 각 기능에 대해 고민해 보고 찾아보는 시간이 많았던 덕분에, Spring에서 제공하는 클래스 및 어노테이션에 대해 조금이나마 더 배울 수 있었습니다. 또한 방대한 프레임워크인 만큼, 해당 프레임워크에 대해 깊게 알수록 개발자가 직접 개발해야 하는 부분은 줄어들고, 더 안정적인 개발이 가능하다는 점을 느꼈습니다. 이를 통해, 개발자로서 학습의 중요성에 대해 다시 한번 상기할 기회가 되었습니다.

&nbsp;&nbsp;이번 프로젝트에서는 처음으로 테스트 코드를 작성하여 각 주요 기능별 테스트를 진행해 보기도 했습니다. Spring 관련 강의를 보거나, 여러 개발 관련 유튜브에서 테스트 코드에 대해 간접적인 지식만을 쌓은 상태로 작성한 테스트 코드이기에, 다소 미흡한 점이 있습니다..
하지만, 개발할 때 확인하지 못했던 예외사항을 테스트를 통해해 깨닫게 되거나, 기존에 개발했던 코드를 수정해야 할 경우에도 기존에 작성한 테스트 코드를 통해 연관된 기능의 정상 여부를 확인하고 수정할 수 있었습니다. 이러한 과정을 통해 더 안정적인 애플리케이션을 개발할 수 있었다고 생각하며, 테스트의 중요성에 대해 몸소 느낄 수 있었습니다.
