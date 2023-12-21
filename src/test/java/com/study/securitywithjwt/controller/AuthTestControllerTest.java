package com.study.securitywithjwt.controller;

import com.study.securitywithjwt.testUtils.customMockUser.WithMockCustomUser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthTestControllerTest {
  private final String URI_FOR_API = "/api";
  private final String URI_FOR_API_ADMIN = "/api/admin";

  @Autowired
  MockMvc mockMvc;


  @Nested
  @WithAnonymousUser
  class AnonymousPermissionTest {
    // get - /api
    @Test
    void getForAllUsers_anonymous_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get(URI_FOR_API))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //post - /api
    @Test
    void postForAuthenticatedUsers_anonymous_unauthorized()throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post(URI_FOR_API))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    //get - /api/admin
    @Test
    void getForAdminAndManager_anonymous_unauthorized() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    //post - /api/admin
    @Test
    void postForAdmin_anonymous_unauthorized() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    //delete /api/admin
    @Test
    void deleteForAdmin_anonymous_unauthorized() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.delete(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
  }

  @Nested
  @WithMockCustomUser(roles = "USER")
  //@WIthMockUser는 UsernamePasswordAuthenticationToken을 Authentication으로 사용해서 500에러 발생.
  //토큰에서 user info 꺼내오는 로직에서 JwtAuthenticationToken을 사용해서, type casting 에러 발생함.
  class UserPermissionTest {
    // get - /api
    @Test
    void getForAllUsers_user_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get(URI_FOR_API))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //post - /api
    @Test
    void postForAuthenticatedUsers_user_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post(URI_FOR_API))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //get - /api/admin
    @Test
    void getForAdminAndManager_user_forbidden() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    //post - /api/admin
    @Test
    void postForAdmin_user_forbidden() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    //delete /api/admin
    @Test
    void deleteForAdmin_user_forbidden() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.delete(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
  }

  @Nested
  @WithMockCustomUser(roles = "MANAGER")
  class ManagerPermissionTest {
    // get - /api
    @Test
    void getForAllUsers_manager_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get(URI_FOR_API))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //post - /api
    @Test
    void postForAuthenticatedUsers_manager_ok()throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post(URI_FOR_API))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //get - /api/admin
    @Test
    void getForAdminAndManager_manager_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //post - /api/admin
    @Test
    void postForAdmin_manager_unauthorized() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    //delete /api/admin
    @Test
    void deleteForAdmin_manager_forbidden() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.delete(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
  }

  @Nested
  @WithMockCustomUser(roles = "ADMIN")
  class AdminPermissionTest {
    // get - /api
    @Test
    void getForAllUsers_admin_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get(URI_FOR_API))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //post - /api
    @Test
    void postForAuthenticatedUsers_admin_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post(URI_FOR_API))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //get - /api/admin
    @Test
    void getForAdminAndManager_admin_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.get(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //post - /api/admin
    @Test
    void postForAdmin_admin_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.post(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }

    //delete /api/admin
    @Test
    void deleteForAdmin_admin_ok() throws Exception {
      mockMvc.perform(MockMvcRequestBuilders.delete(URI_FOR_API_ADMIN))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }
  }
}
