package com.example.mypixel.model.node;

import com.example.mypixel.model.ParameterType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Map;

@MyPixelNode("Combine")
public class CombineNode extends Node {

    @JsonCreator
    public CombineNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, ParameterType> getInputTypes() {
        return Map.of(
                "files_0", ParameterType.FILEPATH_ARRAY.required(),
                "files_1", ParameterType.FILEPATH_ARRAY.optional(),
                "files_2", ParameterType.FILEPATH_ARRAY.optional(),
                "files_3", ParameterType.FILEPATH_ARRAY.optional(),
                "files_4", ParameterType.FILEPATH_ARRAY.optional()
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "files_0", new HashSet<String>(),
                "files_1", new HashSet<String>(),
                "files_2", new HashSet<String>(),
                "files_3", new HashSet<String>(),
                "files_4", new HashSet<String>()
        );
    }

    @Override
    public Map<String, ParameterType> getOutputTypes() {
        return Map.of("files", ParameterType.FILEPATH_ARRAY);
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "category", "IO",
                "description", "Combine multiple data sources into a single source",
                "color", "#AED581",
                "icon", "CombineIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        HashSet<String> files = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            if (inputs.containsKey("files_" + i) && inputs.get("files_" + i) != null) {
                HashSet<String> f = (HashSet<String>) inputs.get("files_" + i);
                files.addAll(f);
            }
        }

        Map<String, Object> outputs;
        outputs = Map.of("files", files);

        return outputs;
    }

    @Override
    public void validate() {

    }
}
