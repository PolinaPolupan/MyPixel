package com.example.mypixel.model.node;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Map;


@MyPixelNode("GaussianBlur")
public class GaussianBlurNode extends Node {

    @JsonCreator
    public GaussianBlurNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return Map.of(
                "files", Parameter.required(ParameterType.FILEPATH_ARRAY),
                "sizeX", Parameter.required(ParameterType.INT),
                "sizeY", Parameter.optional(ParameterType.INT),
                "sigmaX", Parameter.optional(ParameterType.DOUBLE),
                "sigmaY", Parameter.optional(ParameterType.DOUBLE)
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "files", new HashSet<String>(),
                "sizeX", 3,
                "sizeY", 3,
                "sigmaX", 0.0,
                "sigmaY", 0.0
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return Map.of("files", Parameter.required(ParameterType.FILEPATH_ARRAY));
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "category", "Filtering",
                "description", "Blurs an image using a Gaussian kernel",
                "color", "#FF8A65",
                "icon", "BlurIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("files");
        Map<String, Object> outputs;

        int sizeX = (int) inputs.get("sizeX");
        int sizeY = (int) inputs.getOrDefault("sizeY", sizeX);
        double sigmaX = (double) inputs.getOrDefault("sigmaX", 0.0);
        double sigmaY = (double) inputs.getOrDefault("sigmaY", 0.0);

        batchProcessor.processBatches(files,
                filepath -> filteringService.gaussianBlur(filepath, sizeX, sizeY, sigmaX, sigmaY));

        outputs = Map.of("files", files);

        return outputs;
    }

    @Override
    public void validate() {
        int sizeX = (int) inputs.get("sizeX");
        int sizeY = (int) inputs.getOrDefault("sizeY", sizeX);
        double sigmaX = (double) inputs.getOrDefault("sigmaX", 0.0);
        double sigmaY = (double) inputs.getOrDefault("sigmaY", 0.0);

        if (sizeX < 0 || sizeX % 2 == 0) {
            throw new InvalidNodeParameter("SizeX must be positive and odd");
        }
        if (sizeY < 0 || sizeY % 2 == 0) {
            throw new InvalidNodeParameter("SizeY must be positive and odd");
        }
        if (sigmaX < 0) {
            throw new InvalidNodeParameter("SigmaX must be positive");
        }
        if (sigmaY < 0) {
            throw new InvalidNodeParameter("SigmaY must be positive");
        }
    }
}
