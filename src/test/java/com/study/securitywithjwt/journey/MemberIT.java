package com.study.securitywithjwt.journey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.TestConfig;
import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.*;
import com.study.securitywithjwt.jwt.JwtUtils;
import com.study.securitywithjwt.repository.MemberRepository;
import com.study.securitywithjwt.repository.RefreshTokenRepository;
import com.study.securitywithjwt.repository.RoleRepository;
import com.study.securitywithjwt.service.RefreshTokenService;
import com.study.securitywithjwt.utils.member.Gender;
import com.study.securitywithjwt.utils.member.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:/application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig.class)
@Slf4j
public class MemberIT {
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  private JwtUtils jwtUtils;
  @Autowired
  PasswordEncoder passwordEncoder;
  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  RefreshTokenRepository refreshTokenRepository;
  @Autowired
  RefreshTokenService refreshTokenService;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  RoleRepository roleRepository;


  @Test
  void roleUserCrud() {
    /*role user*/
    //sign in test member -> no role -> ROLE_USER assigned
    String signupEmail = "testUser@email.com";
    String signupPassword = "00000000";
    String signupName = "testUser";
    String signupPhone = "01000000000";
    MemberSignupRequestDto signupRequestDto = new MemberSignupRequestDto();
    signupRequestDto.setEmail(signupEmail);
    signupRequestDto.setPassword(signupPassword);
    signupRequestDto.setName(signupName);
    signupRequestDto.setGender(Gender.MALE);
    signupRequestDto.setPhone(signupPhone);

    log.info("sign up request dto : {}", signupRequestDto);

    EntityExchangeResult<MemberSignupResponseDto> signupResponse = webTestClient.post()
        .uri("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(signupRequestDto)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(MemberSignupResponseDto.class)
        .returnResult();

    log.info("sign up response dto : {}", signupResponse.getResponseBody());

    Long testUserMemberId = Objects.requireNonNull(signupResponse.getResponseBody()).getMemberId();
    Role userRole = roleRepository.findByName(UserRole.ROLE_USER).get();
    Member savedMember = memberRepository.findById(testUserMemberId).get();
    assertThat(savedMember.getRoles().size()).isEqualTo(1);
    assertThat(savedMember.getRoles().contains(userRole)).isTrue();


    //sign in test member -> no role -> requested roles assigned

    String signupEmail2 = "testUserWithRoles@email.com";
    String signupPassword2 = "00000000";
    String signupName2 = "testUser";
    String signupPhone2 = "01098761234";
    MemberSignupRequestDto signupRequestDto2 = new MemberSignupRequestDto();
    signupRequestDto2.setEmail(signupEmail2);
    signupRequestDto2.setPassword(signupPassword2);
    signupRequestDto2.setName(signupName2);
    signupRequestDto2.setGender(Gender.MALE);
    signupRequestDto2.setPhone(signupPhone2);

    List<Role> allRolesList = roleRepository.findAll();
    Set<UserRole> userRoleSet = allRolesList.stream().map(Role::getName).collect(Collectors.toSet());

    signupRequestDto2.setUserRoles(userRoleSet);

    log.info("sign up request dto : {}", signupRequestDto2);

    EntityExchangeResult<MemberSignupResponseDto> signupResponse2 = webTestClient.post()
        .uri("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(signupRequestDto2)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(MemberSignupResponseDto.class)
        .returnResult();

    log.info("sign up response dto : {}", signupResponse.getResponseBody());
    Long memberWithMultipleRolesId = Objects.requireNonNull(signupResponse2.getResponseBody()).getMemberId();

    Member memberWithMultipleRoles = memberRepository.findById(memberWithMultipleRolesId).get();
    assertThat(memberWithMultipleRoles.getRoles().size()).isEqualTo(userRoleSet.size());
    assertThat(memberWithMultipleRoles.getRoles()
        .stream().map(Role::getName).collect(Collectors.toSet())).isEqualTo(userRoleSet);














    //login test member
    EntityExchangeResult<LoginResponseDto> loginResponse = webTestClient.post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new LoginRequestDto(signupRequestDto.getEmail(), signupRequestDto.getPassword()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(LoginResponseDto.class)
        .returnResult();

    log.info("login response dto : {} ", loginResponse.getResponseBody());

    String accessTokenWithBearer = String.format("Bearer %s", Objects.requireNonNull(loginResponse.getResponseBody()).getAccessToken());
    String AUTHORIZATION = "Authorization";

    //get test member
    EntityExchangeResult<MemberDto> responseGetMember =
        webTestClient.get().uri("/members/" + testUserMemberId)
            .header(AUTHORIZATION, accessTokenWithBearer)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(MemberDto.class)
            .returnResult();


    log.info("get member id : {} ", testUserMemberId);
    log.info("found member : {}", responseGetMember.getResponseBody());

    Member memberFound = memberRepository.findById(testUserMemberId).get();


    MemberDto responseBody = responseGetMember.getResponseBody();
    assertThat(responseBody).isNotNull();
    assertThat(responseBody.getName()).isEqualTo(memberFound.getName());
    assertThat(responseBody.getPhone()).isEqualTo(memberFound.getPhone());
    assertThat(responseBody.getEmail()).isEqualTo(memberFound.getEmail());
    assertThat(responseBody.getGender()).isEqualTo(memberFound.getGender());
    assertThat(responseBody.getMemberId()).isEqualTo(memberFound.getMemberId());
    assertThat(responseBody.getRoles())
        .isEqualTo(memberFound.getRoles().stream().map(role->role.getName().name()).collect(Collectors.toSet()));

    //get memberList

    int page = 1;
    int size = 10;

    Long countOfAllMember = memberRepository.count();

    webTestClient.get()
        .uri("/members?page=1&size=10")
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.totalElements").isEqualTo(countOfAllMember)
        .jsonPath("$.size").isEqualTo(size)
        .jsonPath("$.number").isEqualTo(page - 1);//Page의 index는 0부터 시작

    //update test member
    String updatePassword = "11111111";
    String updateName = "name123";
    String updateEmail = "update@test.com";
    String updatePhone = "01012381255";
    MemberUpdateRequestDto updateRequestDto = new MemberUpdateRequestDto();
    updateRequestDto.setMemberId(testUserMemberId);
    updateRequestDto.setPassword(updatePassword);
    updateRequestDto.setName(updateName);
    updateRequestDto.setEmail(updateEmail);
    updateRequestDto.setPhone(updatePhone);

    webTestClient.put()
        .uri("/members/" + testUserMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateRequestDto)
        .exchange()
        .expectStatus()
        .isOk();
    Member memberUpdated = memberRepository.findById(testUserMemberId).get();
    assertThat(memberUpdated.getMemberId()).isEqualTo(testUserMemberId);
    assertThat(memberUpdated.getName()).isEqualTo(updateName);
    assertThat(memberUpdated.getEmail()).isEqualTo(updateEmail);
    assertThat(memberUpdated.getPhone()).isEqualTo(updatePhone);
    assertThat(passwordEncoder.matches(updatePassword, memberUpdated.getPassword())).isTrue();
    //udpate other member -> fail

    long differentMemberId = 100L;
    webTestClient.put()
        .uri("/members/" + differentMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateRequestDto)
        .exchange()
        .expectStatus()
        .isForbidden();


    //delete other membr -> fail
    webTestClient.delete()
        .uri("/members/" + differentMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isForbidden();

    //delete test member
    webTestClient.delete()
        .uri("/members/" + testUserMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void roleAdminCrud() {
    /*role admin*/
    //login test admin
    String adminEmail = "admin@test.com";
    String adminPassword = "00000000";
    LoginRequestDto adminLoginRequest = new LoginRequestDto(adminEmail, adminPassword);

    EntityExchangeResult<LoginResponseDto> loginResponse = webTestClient.post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(adminLoginRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(LoginResponseDto.class)
        .returnResult();
    //get test admin
    String accessTokenWithBearer = String.format("Bearer %s", Objects.requireNonNull(loginResponse.getResponseBody()).getAccessToken());
    String AUTHORIZATION = "Authorization";

    long allMemberCount = memberRepository.count();

    //admin(id = 1)을 제외한 아이디 결정
    long randomMemberId =  (long)(Math.random() * allMemberCount-1)+2;
    System.out.println("randomMemberId = " + randomMemberId);
    //get test member
    webTestClient.get().uri("/members/" + randomMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk();

    //get memberList
    int page = 1;
    int size = 10;


    webTestClient.get()
        .uri("/members?page=1&size=10")
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.totalElements").isEqualTo(allMemberCount)
        .jsonPath("$.size").isEqualTo(size)
        .jsonPath("$.number").isEqualTo(page - 1);//Page의 index는 0부터 시작

    //update test admin
    long adminId = 1L;


    String updatePassword = "44221133";
    String updateName = "nameTest";
    String updateEmail = "updateAAA@test.com";
    String updatePhone = "01098765432";
    MemberUpdateRequestDto updateRequestDto = new MemberUpdateRequestDto();
    updateRequestDto.setMemberId(randomMemberId);
    updateRequestDto.setPassword(updatePassword);
    updateRequestDto.setName(updateName);
    updateRequestDto.setEmail(updateEmail);
    updateRequestDto.setPhone(updatePhone);

    webTestClient.put()
        .uri("/members/" + adminId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateRequestDto)
        .exchange()
        .expectStatus()
        .isOk();
    Member memberUpdated = memberRepository.findById(adminId).get();

    assertThat(memberUpdated.getMemberId()).isEqualTo(adminId);
    assertThat(memberUpdated.getName()).isEqualTo(updateName);
    assertThat(memberUpdated.getEmail()).isEqualTo(updateEmail);
    assertThat(memberUpdated.getPhone()).isEqualTo(updatePhone);
    assertThat(passwordEncoder.matches(updatePassword, memberUpdated.getPassword())).isTrue();



    //udpate other member -> ok

    String updatePassword2 = "22222222";
    String updateName2 = "name456123";
    String updateEmail2 = "update22@test.com";
    String updatePhone2 = "01088888888";
    MemberUpdateRequestDto updateRequestDto2 = new MemberUpdateRequestDto();
    updateRequestDto2.setMemberId(randomMemberId);
    updateRequestDto2.setPassword(updatePassword2);
    updateRequestDto2.setName(updateName2);
    updateRequestDto2.setEmail(updateEmail2);
    updateRequestDto2.setPhone(updatePhone2);

    webTestClient.put()
        .uri("/members/" + randomMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateRequestDto2)
        .exchange()
        .expectStatus()
        .isOk();
    Member memberUpdated2 = memberRepository.findById(randomMemberId).get();

    assertThat(memberUpdated2.getMemberId()).isEqualTo(randomMemberId);
    assertThat(memberUpdated2.getName()).isEqualTo(updateName2);
    assertThat(memberUpdated2.getEmail()).isEqualTo(updateEmail2);
    assertThat(memberUpdated2.getPhone()).isEqualTo(updatePhone2);
    assertThat(passwordEncoder.matches(updatePassword2, memberUpdated2.getPassword())).isTrue();

    //delete other membr -> ok
    webTestClient.delete()
        .uri("/members/" + randomMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk();

    //delete test admin
    webTestClient.delete()
        .uri("/members/" + adminId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk();

  }
}
