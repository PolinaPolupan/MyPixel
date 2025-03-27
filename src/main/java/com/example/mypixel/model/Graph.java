package com.example.mypixel.model;

import com.example.mypixel.model.node.Node;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.*;

@Getter
@AllArgsConstructor
public class Graph {
    List<Node> nodes;

    public Iterator<Node> iterator() {
        return new GraphIterator(this);
    }

    public List<Long> getNodeOutputs(Node node) {
        List<Long> outputs = new ArrayList<>();

        for (Node n: nodes) {
            for (Object param: n.getInputs().values()) {
                if (param instanceof NodeReference) {
                    if (((NodeReference) param).getNodeId().equals(node.getId())) outputs.add(n.getId());
                }
            }
        }
        return outputs;
    }
}
