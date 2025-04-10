package com.example.mypixel.service;

import com.example.mypixel.exception.InvalidNodeParameter;
import com.example.mypixel.model.*;
import com.example.mypixel.model.node.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NodeProcessorService {

    private final AutowireCapableBeanFactory beanFactory;
    // Hash map to store nodes and their corresponding outputs
    private final Map<Long, Map<String, Object>> nodeOutputs = new HashMap<>();
    private final Map<Long, Node> nodeMap = new HashMap<>();
    private final StorageService tempStorageService;
    private final StorageService storageService;

    @Autowired
    public NodeProcessorService(
            AutowireCapableBeanFactory beanFactory,
            @Qualifier("storageService") StorageService storageService,
            @Qualifier("tempStorageService") StorageService tempStorageService
    ) {
        this.beanFactory = beanFactory;
        this.tempStorageService = tempStorageService;
        this.storageService = storageService;
    }

    public void processNode(Node node) {
        beanFactory.autowireBean(node);
        nodeMap.put(node.getId(), node);
        nodeOutputs.computeIfAbsent(node.getId(), k -> new HashMap<>());
        resolveInputs(node);
        node.validate();
        nodeOutputs.put(node.getId(), node.exec());
        log.info(nodeOutputs.toString());
    }

    private Object resolveReference(NodeReference reference) {
        Long id = reference.getNodeId();
        String output = reference.getOutputName();

        if (!nodeMap.containsKey(id)) {
            throw new InvalidNodeParameter("Invalid node reference: Node with id " +
                    id + " is not found. Please ensure the node id is correct.");
        }

        if (!nodeMap.get(id).getOutputTypes().containsKey(output)) {
            throw new InvalidNodeParameter("Invalid node reference: Node with id "
                    + id + " does not contain output '" + output
                    + "'. Available outputs are: " + nodeMap.get(id).getOutputTypes().keySet());
        }

        return nodeOutputs.get(id).get(output);
    }

    private Object castTypes(Object value, ParameterType requiredType) {
        if (value == null) {
            throw new InvalidNodeParameter("Cannot cast null to " + requiredType + " type");
        }
        return switch (requiredType) {
            case FLOAT -> value instanceof Number ? ((Number) value).floatValue() : (float) value;
            case INT -> value instanceof Number ? ((Number) value).intValue() : (int) value;
            case DOUBLE -> value instanceof Number ? ((Number) value).doubleValue() : (double) value;
            case STRING -> (String) value;
            case FILENAMES_ARRAY -> {
                List<String> files = new ArrayList<>();
                for (String file: (List<String>) value) {
                    String temp;
                    if (storageService.fileExists(file)) { // Find in the main storage
                        temp = tempStorageService.createTempFileFromResource(storageService.loadAsResource(file));
                    } else { // Find in the cache
                        temp = tempStorageService.createTempFileFromResource(tempStorageService.loadAsResource(file));
                    }
                    files.add(temp);
                }
                yield files;
            }
            case STRING_ARRAY -> (List<String>) value;
        };
    }

    private void resolveInputs(Node node) {
        Map<String, Object> resolvedInputs = new HashMap<>();

        for (String key: node.getInputTypes().keySet()) {
            // If the user's inputs don't contain one of the parameters
            if (!node.getInputs().containsKey(key)) {
                // If it is required - throw an exception
                if (node.getInputTypes().get(key).isRequired()) {
                    throw new InvalidNodeParameter("Required input " + key
                            + " is not provided for the node with id " + node.getId());
                } else { // Omit, continue on processing other inputs
                    continue;
                }

            }

            Object input = node.getInputs().get(key);
            ParameterType requiredType = node.getInputTypes().get(key);

            if (input instanceof NodeReference) {
                input = resolveReference((NodeReference) input);
            }

            // Cast to required type
            try {
                input = castTypes(input, requiredType);
            } catch (ClassCastException e) {
                throw new InvalidNodeParameter(
                        "Invalid input parameter '" + key + "' to the node with id " +
                                node.getId() + ": cannot cast " + input.getClass().getSimpleName() +
                                " to " + requiredType + " type"
                );
            }

            resolvedInputs.put(key, input);
        }

        node.setInputs(resolvedInputs);
    }

    public void clear() {
        nodeOutputs.clear();
        nodeMap.clear();
        tempStorageService.deleteAll();
        tempStorageService.init();
    }
}
