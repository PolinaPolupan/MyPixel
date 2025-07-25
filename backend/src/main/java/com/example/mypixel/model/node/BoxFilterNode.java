package com.example.mypixel.model.node;


import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.Vector2D;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Map;

@MyPixelNode("BoxFilter")
public class BoxFilterNode extends Node {

    @JsonCreator
    public BoxFilterNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return Map.of(
                "files", Parameter.required(ParameterType.FILEPATH_ARRAY),
                "ddepth", Parameter.required(ParameterType.INT),
                "ksize", Parameter.required(ParameterType.VECTOR2D)
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "files", new HashSet<String>(),
                "ddepth", 0,
                "ksize", new Vector2D<>(1, 1)
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
                "description", "Blurs an image using the specified kernel size",
                "color", "#FF8A65",
                "icon", "BlurIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = (HashSet<String>) inputs.get("files");
        Map<String, Object> outputs;

        Vector2D<Number> ksize = (Vector2D<Number>) inputs.get("ksize");
        int ddepth = (int) inputs.get("ddepth");

        batchProcessor.processBatches(files,
                filepath -> filteringService.boxFilter(filepath, ddepth, ksize));

        outputs = Map.of("files", files);

        return outputs;
    }

    @Override
    public void validate() {
        Vector2D<Number> ksize = (Vector2D<Number>) inputs.get("ksize");
        Integer ksizeX = ksize.getX().intValue();
        Integer ksizeY = ksize.getY().intValue();

        if (ksizeX < 1 || ksizeY < 1) {
            throw new InvalidNodeParameter("KSize must be greater than 0");
        }
    }
}
