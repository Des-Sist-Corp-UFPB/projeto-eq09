package br.ufpb.dsc.mercado.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    private S3StorageService s3StorageService;

    @BeforeEach
    void setUp() {
        s3StorageService = new S3StorageService(s3Client, "test-bucket", "http://localhost:9000");
    }

    @Test
    void testUploadFile_Success_WithExtension() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("avatar.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(100L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("fake image".getBytes()));

        String url = s3StorageService.uploadFile(file);

        assertNotNull(url);
        assertTrue(url.startsWith("http://localhost:9000/test-bucket/"));
        assertTrue(url.endsWith(".png"));

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_Success_NoExtension() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("avatar");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(50L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("fake jpeg".getBytes()));

        String url = s3StorageService.uploadFile(file);

        assertNotNull(url);
        assertTrue(url.startsWith("http://localhost:9000/test-bucket/"));
        
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_Success_NullFilename() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getContentType()).thenReturn("image/gif");
        when(file.getSize()).thenReturn(10L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("fake gif".getBytes()));

        String url = s3StorageService.uploadFile(file);

        assertNotNull(url);
        assertTrue(url.startsWith("http://localhost:9000/test-bucket/"));

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_Failure_IOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("avatar.png");
        when(file.getInputStream()).thenThrow(new IOException("Read error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            s3StorageService.uploadFile(file);
        });

        assertEquals("Erro ao fazer upload do arquivo para o S3/MinIO", exception.getMessage());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
