package ru.netology.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test void createsAndListsTasks() throws Exception {
        mvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Buy milk\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(jsonPath("$.title").value("Buy milk"))
                .andExpect(jsonPath("$.completed").value(false));
        mvc.perform(get("/tasks")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Buy milk")));
    }

    @Test void updatesAndDeletesTasks() throws Exception {
        String response = mvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Draft\"}"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(response).get("id").asLong();
        mvc.perform(put("/tasks/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Done\",\"completed\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Done"))
                .andExpect(jsonPath("$.completed").value(true));
        mvc.perform(delete("/tasks/" + id)).andExpect(status().isNoContent());
    }

    @Test void validatesInputAndReturns404ForMissingTask() throws Exception {
        mvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\" \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/tasks/99999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Missing\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/tasks/99999")).andExpect(status().isNotFound());
    }
}
