package com.acme.salary.shared;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.acme.salary.auth.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
@WebMvcTest(AuthController.class) @Import(SecurityConfig.class) @TestPropertySource(properties={"app.demo.username=hr@acme.test","app.demo.password=ChangeMe123!","app.allowed-origins=http://localhost:4200"})
class SecurityConfigTest {
 @Autowired MockMvc mvc;
 @Test void rejectsAnonymousRequests()throws Exception{mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());}
 @Test void returnsAuthenticatedHrUser()throws Exception{mvc.perform(get("/api/v1/auth/me").with(httpBasic("hr@acme.test","ChangeMe123!"))).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("HR_MANAGER"));}
}
