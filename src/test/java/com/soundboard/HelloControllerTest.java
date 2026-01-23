package com.soundboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest /// This annotation is used to signify this is a test case
@AutoConfigureMockMvc /// This annotation is used to auto-configure MockMvc instance, MockMvc is a class that provides support for Spring MVC testing
/// MVC is Model-View-Controller, a software design pattern commonly used for developing
/// user interfaces that divides the related program logic into three interconnected elements -> Model, View, Controller
/// Model represents the data and business logic, View represents the user interface, Controller handles user input and interactions
public class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getHello() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Greetings from SpringBoot Soundboard!"))
        );
    }
}
