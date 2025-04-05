import {
  useReactFlow,
} from '@xyflow/react';

import LabeledHandle from './LabeledHandle';
import NodeHeader from './NodeHeader';
import InputHandle from './InputHandle';
 
function FloorNode({ id, data }) {
  const { updateNodeData } = useReactFlow();
 
  return (
    <div style={{ 
      minWidth: '100px'
    }}>
      <NodeHeader title={"Floor"}/>
      <InputHandle 
        id={id}
        data={data}
        title="Floor"
        handleId="number"
        handleLabel="Number"
      />
      <LabeledHandle 
        label="Number"
        type="source"
        position="right" 
        id="number"
      />
    </div>
  );
}

export default FloorNode;