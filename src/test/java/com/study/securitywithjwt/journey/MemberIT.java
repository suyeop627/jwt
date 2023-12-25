package com.study.securitywithjwt.journey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.securitywithjwt.TestConfig;
import com.study.securitywithjwt.domain.Member;
import com.study.securitywithjwt.domain.Role;
import com.study.securitywithjwt.dto.*;
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
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfig.class)
@Slf4j
public class MemberIT {
  @Autowired
  ObjectMapper objectMapper;
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

    //base account of roleUserCrud test
    String roleUserEmail = "testUser@email.com";
    String roleUserPassword = "00000000";
    String roleUserName = "testUser";
    String roleUserPhone = "01000000000";
    MemberSignupRequestDto roleUserSignupRequest = new MemberSignupRequestDto();
    roleUserSignupRequest.setEmail(roleUserEmail);
    roleUserSignupRequest.setPassword(roleUserPassword);
    roleUserSignupRequest.setName(roleUserName);
    roleUserSignupRequest.setGender(Gender.MALE);
    roleUserSignupRequest.setPhone(roleUserPhone);

    //sign up member for test - no role set - ROLE_USER assigned
    MemberSignupResponseDto roleUserSignupResponse = webTestClient.post()
        .uri("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(roleUserSignupRequest)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(MemberSignupResponseDto.class)
        .returnResult()
        .getResponseBody();


    //compare sign up request with sign up response
    Long roleUserMemberId = Objects.requireNonNull(roleUserSignupResponse).getMemberId();
    Role userRole = roleRepository.findByName(UserRole.ROLE_USER).get();

    Member savedRoleUserMember = memberRepository.findById(roleUserMemberId).get();

    assertThat(savedRoleUserMember.getRoles().size()).isEqualTo(1);
    assertThat(savedRoleUserMember.getRoles().contains(userRole)).isTrue();


    //login test member(ROLE_USER)
    LoginResponseDto loginResponseOfRoleUser = webTestClient.post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new LoginRequestDto(roleUserSignupRequest.getEmail(), roleUserSignupRequest.getPassword()))
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(LoginResponseDto.class)
        .returnResult()
        .getResponseBody();

    String accessTokenWithBearer = String.format("Bearer %s", Objects.requireNonNull(loginResponseOfRoleUser).getAccessToken());
    String AUTHORIZATION = "Authorization";

    //get test member - get memberId same with login member id
    MemberDto memberDtoFound =
        webTestClient.get().uri("/members/" + roleUserMemberId)
            .header(AUTHORIZATION, accessTokenWithBearer)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(MemberDto.class)
            .returnResult()
            .getResponseBody();


    log.info("get member id : {} ", roleUserMemberId);
    log.info("found member : {}", memberDtoFound);

    //compare member from response with saved member(saved when member request sign up)
    assertThat(memberDtoFound).isNotNull();
    assertThat(memberDtoFound.getName()).isEqualTo(savedRoleUserMember.getName());
    assertThat(memberDtoFound.getPhone()).isEqualTo(savedRoleUserMember.getPhone());
    assertThat(memberDtoFound.getEmail()).isEqualTo(savedRoleUserMember.getEmail());
    assertThat(memberDtoFound.getGender()).isEqualTo(savedRoleUserMember.getGender());
    assertThat(memberDtoFound.getMemberId()).isEqualTo(savedRoleUserMember.getMemberId());
    assertThat(memberDtoFound.getRoles())
        .isEqualTo(savedRoleUserMember
            .getRoles().stream()
            .map(role->role.getName().name())
            .collect(Collectors.toSet()));


    //getMember - nonexistentMemberId - 404
    long countAllMember = memberRepository.count();
    long nonexistentMemberId = countAllMember + 10;
    webTestClient.get().uri("/members/" + nonexistentMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isNotFound();




    //get memberList - validState - return Page of selected
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
        .jsonPath("$.number").isEqualTo(page - 1);//index of 'Page' starts from '0'


    //update test member - update own data with role user - 200
    String changedPassword = "11111111";
    String changedName = "name123";
    String changedEmail = "update@test.com";
    String changedPhone = "01012381255";
    MemberUpdateRequestDto updateRequestDto = new MemberUpdateRequestDto();
    updateRequestDto.setMemberId(roleUserMemberId);
    updateRequestDto.setPassword(changedPassword);
    updateRequestDto.setName(changedName);
    updateRequestDto.setEmail(changedEmail);
    updateRequestDto.setPhone(changedPhone);

    MemberDto updatedMemberDtoOfResponse = webTestClient.put()
        .uri("/members/" + roleUserMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateRequestDto)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(MemberDto.class)
        .returnResult()
        .getResponseBody();

    Member savedRoleUserAfterUpdate = memberRepository.findById(roleUserMemberId).get();

    assertThat(updatedMemberDtoOfResponse).isNotNull();
    assertThat(updatedMemberDtoOfResponse.getMemberId()).isEqualTo(roleUserMemberId);
    assertThat(updatedMemberDtoOfResponse.getName()).isEqualTo(changedName);
    assertThat(updatedMemberDtoOfResponse.getEmail()).isEqualTo(changedEmail);
    assertThat(updatedMemberDtoOfResponse.getPhone()).isEqualTo(changedPhone);
    assertThat(passwordEncoder.matches(changedPassword, savedRoleUserAfterUpdate.getPassword())).isTrue();



    //udpate other member - request Member id not same with login member id(in token) - fail (403)
    long differentMemberId = 100L;
    webTestClient.put()
        .uri("/members/" + differentMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateRequestDto)
        .exchange()
        .expectStatus()
        .isForbidden();


    //delete other membr - request Member id not same with login member id(in token) - fail (403)
    webTestClient.delete()
        .uri("/members/" + differentMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isForbidden();

    //delete - own member - success (200)
    webTestClient.delete()
        .uri("/members/" + roleUserMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void roleAdminCrud() {
    /*role admin*/

    //sign up test admin member -> set ROLE_ADMIN, ROLE_MANAGER -> requested roles assigned
    String roleAdminEmail = "testUserWithRoles@email.com";
    String roleAdminPassword = "00000000";
    String roleAdminName = "testUser";
    String roleAdminPhone = "01098761234";
    MemberSignupRequestDto roleAdminSignupRequest = new MemberSignupRequestDto();
    roleAdminSignupRequest.setEmail(roleAdminEmail);
    roleAdminSignupRequest.setPassword(roleAdminPassword);
    roleAdminSignupRequest.setName(roleAdminName);
    roleAdminSignupRequest.setGender(Gender.MALE);
    roleAdminSignupRequest.setPhone(roleAdminPhone);

    List<Role> allRolesList = roleRepository.findAll();
    Set<UserRole> userRoleSet = allRolesList.stream()
        .map(Role::getName)
        .filter(name -> name !=UserRole.ROLE_USER)
        .collect(Collectors.toSet());

    roleAdminSignupRequest.setUserRoles(userRoleSet);

    MemberSignupResponseDto roleAdminSignupResponse = webTestClient.post()
        .uri("/members")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(roleAdminSignupRequest)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(MemberSignupResponseDto.class)
        .returnResult()
        .getResponseBody();

    //compare sign up request with sign up response
    log.info("sign up response dto : {}", roleAdminSignupResponse);
    Long roleAdminMemberId = Objects.requireNonNull(roleAdminSignupResponse).getMemberId();
    Member savedRoleAdminMember = memberRepository.findById(roleAdminMemberId).get();

    assertThat(savedRoleAdminMember.getRoles().size()).isEqualTo(userRoleSet.size());
    assertThat(savedRoleAdminMember
        .getRoles().stream()
        .map(Role::getName)
        .collect(Collectors.toSet())).isEqualTo(userRoleSet);

    //login test admin
    LoginRequestDto adminLoginRequest = new LoginRequestDto(roleAdminEmail, roleAdminPassword);

    LoginResponseDto adminLoginResponse = webTestClient.post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(adminLoginRequest)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(LoginResponseDto.class)
        .returnResult()
        .getResponseBody();


    //getMember - ok
    String accessTokenWithBearer = String.format("Bearer %s", Objects.requireNonNull(adminLoginResponse).getAccessToken());
    String AUTHORIZATION = "Authorization";

    webTestClient.get().uri("/members/" + roleAdminMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk();


    //getMember - nonexistentMemberId - 404
    long countAllMember = memberRepository.count();
    long nonexistentMemberId = countAllMember + 10;
    webTestClient.get().uri("/members/" + nonexistentMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isNotFound();

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
        .jsonPath("$.totalElements").isEqualTo(countAllMember)
        .jsonPath("$.size").isEqualTo(size)
        .jsonPath("$.number").isEqualTo(page - 1);//Page의 index는 0부터 시작

    //update test admin

    String changedPassword = "44221133";
    String changedName = "nameTest";
    String changedEmail = "updateAAA@test.com";
    String changedPhone = "01098765432";
    MemberUpdateRequestDto updateRequestDto = new MemberUpdateRequestDto();
    updateRequestDto.setMemberId(roleAdminMemberId);
    updateRequestDto.setPassword(changedPassword);
    updateRequestDto.setName(changedName);
    updateRequestDto.setEmail(changedEmail);
    updateRequestDto.setPhone(changedPhone);


    //update request = admin own data - ok (200)
    webTestClient.put()
        .uri("/members/" + roleAdminMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateRequestDto)
        .exchange()
        .expectStatus()
        .isOk();
    Member memberUpdated = memberRepository.findById(roleAdminMemberId).get();

    assertThat(memberUpdated.getMemberId()).isEqualTo(roleAdminMemberId);
    assertThat(memberUpdated.getEmail()).isEqualTo(changedEmail);
    assertThat(memberUpdated.getName()).isEqualTo(changedName);
    assertThat(memberUpdated.getPhone()).isEqualTo(changedPhone);
    assertThat(passwordEncoder.matches(changedPassword, memberUpdated.getPassword())).isTrue();



    //udpate request - other member update with admin role - ok(200)
    long otherMemberId = 3L; //3L is other role_user member saved in db by TestCofig
    String changedPassword2 = "22222222";
    String changedName2 = "name456123";
    String changedEmail2 = "update22@test.com";
    String changedPhone2 = "01088888888";
    MemberUpdateRequestDto updateRequestByAdmin = new MemberUpdateRequestDto();
    updateRequestByAdmin.setMemberId(otherMemberId);
    updateRequestByAdmin.setPassword(changedPassword2);
    updateRequestByAdmin.setName(changedName2);
    updateRequestByAdmin.setEmail(changedEmail2);
    updateRequestByAdmin.setPhone(changedPhone2);

    webTestClient.put()
        .uri("/members/" + otherMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(updateRequestByAdmin)
        .exchange()
        .expectStatus()
        .isOk();
    Member updatedMemberByAdmin = memberRepository.findById(otherMemberId).get();

    assertThat(updatedMemberByAdmin.getMemberId()).isEqualTo(otherMemberId);
    assertThat(updatedMemberByAdmin.getName()).isEqualTo(changedName2);
    assertThat(updatedMemberByAdmin.getEmail()).isEqualTo(changedEmail2);
    assertThat(updatedMemberByAdmin.getPhone()).isEqualTo(changedPhone2);
    assertThat(passwordEncoder.matches(changedPassword2, updatedMemberByAdmin.getPassword())).isTrue();

    //delete other membr - ok(200)
    webTestClient.delete()
        .uri("/members/" + otherMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk();

    //delete test admin by self - ok(200)
    webTestClient.delete()
        .uri("/members/" + roleAdminMemberId)
        .header(AUTHORIZATION, accessTokenWithBearer)
        .exchange()
        .expectStatus()
        .isOk();

  }
}
