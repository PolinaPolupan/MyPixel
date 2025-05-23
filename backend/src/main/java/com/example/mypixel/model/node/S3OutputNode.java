package com.example.mypixel.model.node;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@MyPixelNode("S3Output")
public class S3OutputNode extends Node {

    @JsonCreator
    public S3OutputNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return Map.of(
                "files", Parameter.required(ParameterType.FILEPATH_ARRAY),
                "access_key_id", Parameter.required(ParameterType.STRING),
                "secret_access_key", Parameter.required(ParameterType.STRING),
                "region", Parameter.required(ParameterType.STRING),
                "bucket", Parameter.required(ParameterType.STRING),
                "endpoint", Parameter.optional(ParameterType.STRING),
                "folder", Parameter.optional(ParameterType.STRING)
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "files", new HashSet<>(),
                "access_key_id", "",
                "secret_access_key", "",
                "region", "",
                "bucket", "",
                "folder", ""
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return Map.of();
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "category", "IO",
                "description", "Output files to S3",
                "color", "#AED581",
                "icon", "OutputIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("files");
        Map<String, Object> outputs = Map.of();

        String accessKey = (String) inputs.get("access_key_id");
        String secretKey = (String) inputs.get("secret_access_key");
        String regionName = (String) inputs.get("region");
        String bucket = (String) inputs.get("bucket");
        String endpoint = (String) inputs.get("endpoint");
        String folder = (String) inputs.getOrDefault("folder", "");

        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        S3ClientBuilder clientBuilder = S3Client.builder()
                .region(Region.of(regionName))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));

        if (endpoint != null && !endpoint.isEmpty()) {
            clientBuilder.endpointOverride(URI.create(endpoint))
                    .forcePathStyle(true);
        }

        try (S3Client s3Client = clientBuilder.build()) {
            batchProcessor.processBatches(files, file -> {
                String filename = fileHelper.extractFilename(file);
                Map<String, String> metadata = new HashMap<>();

                s3Client.putObject(request ->
                                request
                                        .bucket(bucket)
                                        .key(folder + "/" + filename)
                                        .metadata(metadata),
                        Path.of(file));
            });
        }

        return outputs;
    }

    @Override
    public void validate() {
        if (inputs.get("access_key_id").toString().isEmpty()) {
            throw new InvalidNodeParameter("Access key ID cannot be blank.");
        }
        if (inputs.get("secret_access_key").toString().isEmpty()) {
            throw new InvalidNodeParameter("Secret cannot be blank.");
        }
        if (inputs.get("region").toString().isEmpty()) {
            throw new InvalidNodeParameter("Region cannot be blank.");
        }
        if (inputs.get("bucket").toString().isEmpty()) {
            throw new InvalidNodeParameter("Bucket cannot be blank.");
        }
    }
}
