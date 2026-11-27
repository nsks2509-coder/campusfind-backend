package com.campusfind.backend.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.campusfind.backend.claim.ClaimRepository;
import com.campusfind.backend.item.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:campusfind-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ApiControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ClaimRepository claimRepository;
    @Autowired private ItemRepository itemRepository;

    @BeforeEach
    void cleanDatabase() {
        claimRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void healthEndpointReportsServiceUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void itemCreationDoesNotExposeReporterEmail() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.contactEmail").doesNotExist());
    }

    @Test
    void claimProofCannotBeReadThroughPublicApi() throws Exception {
        String itemJson = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemPayload()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode item = objectMapper.readTree(itemJson);
        long itemId = item.get("id").asLong();

        mockMvc.perform(post("/api/items/{itemId}/claims", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "claimantName": "Claimant",
                                  "claimantEmail": "claimant@example.com",
                                  "message": "The item has a small scratch under the base."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimantEmail").doesNotExist());

        mockMvc.perform(get("/api/items/{itemId}/claims", itemId))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void invalidItemPayloadIsRejected() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "type": "FOUND",
                                  "category": "Other",
                                  "description": "Description",
                                  "location": "Library",
                                  "contactName": "Student",
                                  "contactEmail": "not-an-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    private String itemPayload() {
        return """
                {
                  "title": "Blue bottle",
                  "type": "FOUND",
                  "category": "Accessories",
                  "description": "A blue bottle with a sticker.",
                  "location": "Central Library",
                  "contactName": "Reporter",
                  "contactEmail": "reporter@example.com"
                }
                """;
    }
}
