package br.ufpb.dsc.mercado.controller;

import br.ufpb.dsc.mercado.service.S3StorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3StorageService storageService;

    @Test
    void testUploadEndpointRequiresAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake image data".getBytes()
        );

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isForbidden()); // Requer autenticação
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUploadEndpointWithAuthenticationAndValidImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "fake image data".getBytes()
        );

        Mockito.when(storageService.uploadFile(Mockito.any())).thenReturn("http://s3-mock/eq09/test.jpg");

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://s3-mock/eq09/test.jpg"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUploadEndpointRejectsEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[0]
        );

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O arquivo não pode ser vazio"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUploadEndpointRejectsInvalidContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "fake text data".getBytes()
        );

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Apenas imagens (JPEG, PNG, GIF, WebP) são permitidas"));
    }
}
