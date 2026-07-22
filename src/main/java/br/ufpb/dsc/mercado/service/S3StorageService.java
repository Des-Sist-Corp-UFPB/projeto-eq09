package br.ufpb.dsc.mercado.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String publicEndpoint;

    public S3StorageService(
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.s3.public-endpoint}") String publicEndpoint) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicEndpoint = publicEndpoint;
    }

    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String key = UUID.randomUUID().toString() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ) // Torna o arquivo público para visualização no navegador
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Retorna a URL pública estruturada
            // Ex: https://s3.dsc.rodrigor.com/eq09/uuid-filename.jpg
            return publicEndpoint + "/" + bucket + "/" + key;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao fazer upload do arquivo para o S3/MinIO", e);
        }
    }
}
