package ec.utb.WebLibraryProject.AppUserControllerTests;
import ec.utb.WebLibraryProject.entity.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestEntityManager
@Transactional
public class AppUserControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private TestEntityManager em;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    //302, OK!
    @Test
    public void nothingInputShouldReturn_302() throws Exception{
        mockMvc.perform(get("/loans/{email}", " "))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/accessDenied"));
    }

    //200, OK!
    @Test
    @WithMockUser(username = "BenjaminEBoson@Gmail.com", authorities = { "ADMIN", "USER" })
    public void EmailInputShouldReturn_200() throws Exception{
        mockMvc.perform(get("/loans/{email}", "BenjaminEBoson@Gmail.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("loans-view"))
                .andExpect(model().attributeExists("loanList"));
    }

    //302, OK!
    @Test
    public void InvalidEmailInputShouldReturn_302() throws Exception{
        mockMvc.perform(get("/loans/{email}", "cheng.tao86@gmail.com"))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/accessDenied"));
    }


    //200, OK!
    @Test
    @WithMockUser(username = "BenjaminEBoson@Gmail.com", authorities = { "ADMIN", "USER" })
    public void inputEmptyShouldReturnBookList_200() throws Exception{
        mockMvc.perform(get("/books"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("books-view"))
                .andExpect(model().attributeExists("bookList"));
    }

    //200, OK!
    @Test
    public void loginReturnSuccess_200() throws Exception{
        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    //200, OK!
    @Test
    public void AccessDeniedReturnSuccess_200() throws Exception{
        mockMvc.perform(get("/accessDenied"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("access-denied"));
    }


    //OK!
    @Test
    @WithMockUser(username = "BenjaminEBoson@Gmail.com", authorities = { "ADMIN", "USER" })
//    @WithAnonymousUser
    public void wrongEndDateReturnError() throws Exception{
        mockMvc.perform(post("/create/loan/process")
                .param("startDate", "2020-04-08")//String - LocalDate
                .param("endDate", "2020-04-07")
                .param("appUserEmail", "BenjaminEBoson@Gmail.com")
                .param("bookId", "1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("form"))
                .andExpect(model().attributeHasFieldErrors("form", "endDate"));
    }

    //OK!
    @Test
    @WithMockUser(username = "BenjaminEBoson@Gmail.com", authorities = { "ADMIN", "USER" })
//    @WithAnonymousUser
    public void wrongStartDateReturnError() throws Exception{
        mockMvc.perform(post("/create/loan/process")
                .param("startDate", "2020-04-01")//String - LocalDate
                .param("endDate", "2020-04-09")
                .param("appUserEmail", "BenjaminEBoson@Gmail.com")
                .param("bookId", "1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("form"))
                .andExpect(model().attributeHasFieldErrors("form", "startDate"));
    }

    //Expected 200 Actual 403
    @Test
    @WithMockUser(username = "BenjaminEBoson@Gmail.com", authorities = { "ADMIN", "USER" })
//    @WithAnonymousUser
    public void postReturnNoError() throws Exception{
        mockMvc.perform(post("/create/loan/process")
                .param("startDate", "2020-04-09")//String - LocalDate
                .param("endDate", "2020-04-16")
                .param("appUserEmail", "BenjaminEBoson@Gmail.com")
                .param("bookId", "1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors());
    }

    //java.lang.IllegalArgumentException
    @Test
    @WithAnonymousUser
    public void ReturnBookReturnSuccess() throws Exception{
        em.persist(new Loan(LocalDate.parse("2020-04-08"), LocalDate.parse("2020-04-10")));
        mockMvc.perform(get("/loans/return/{id}", 1))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/index"));
        em.flush();
    }

}
