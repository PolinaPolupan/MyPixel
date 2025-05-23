package com.example.mypixel.model.node;

import com.example.mypixel.model.Parameter;
import com.example.mypixel.model.ParameterType;
import com.example.mypixel.model.Vector2D;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import java.util.Map;

@MyPixelNode("Vector2D")
public class Vector2DNode extends Node {

    @JsonCreator
    public Vector2DNode(
            @JsonProperty("id") @NonNull Long id,
            @JsonProperty("type") @NonNull String type,
            @JsonProperty("inputs") Map<String, Object> inputs) {
        super(id, type, inputs);
    }

    @Override
    public Map<String, Parameter> getInputTypes() {
        return Map.of(
                "x", Parameter.required(ParameterType.DOUBLE), // Any numerical type can be used here
                "y", Parameter.required(ParameterType.DOUBLE)
        );
    }

    @Override
    public Map<String, Object> getDefaultInputs() {
        return Map.of(
                "x", 0.0,
                "y", 0.0
        );
    }

    @Override
    public Map<String, Parameter> getOutputTypes() {
        return Map.of("vector2D", Parameter.required(ParameterType.VECTOR2D));
    }

    @Override
    public Map<String, String> getDisplayInfo() {
        return Map.of(
                "category", "Types",
                "description", "Creates a 2D vector",
                "color", "#FF8A65",
                "icon", "Vector2DIcon"
        );
    }

    @Override
    public Map<String, Object> exec() {
        Number x = (Number) inputs.get("x");
        Number y = (Number) inputs.get("y");

        Vector2D<Number> vector2D = new Vector2D<>(x, y);

        return Map.of("vector2D", vector2D);
    }

    @Override
    public void validate() {
    }
}
