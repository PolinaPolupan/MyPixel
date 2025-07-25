import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';
   
export default function GaussianBlur({ id, data }) {
  return (
    <div style={{
      minWidth: '100px'
    }}>
      <NodeHeader title={"Gaussian blur"}/>
      <LabeledHandle
        label="Files"
        type="target"
        position="left"
        id="files"
        parameterType="FILENAMES_ARRAY"
      />
      <InputHandle
        id={id}
        data={data}
        handleId="sizeX"
        handleLabel="SizeX"
        parameterType="INT"
      />
      <InputHandle
        id={id}
        data={data}
        handleId="sizeY"
        handleLabel="SizeY"
        parameterType="INT"
      />
      <InputHandle
        id={id}
        data={data}
        handleId="sigmaX"
        handleLabel="SigmaX"
        parameterType="DOUBLE"
        type = "number"
      />
      <InputHandle
        id={id}
        data={data}
        handleId="sigmaY"
        handleLabel="SigmaY"
        parameterType="DOUBLE"
        type = "number"
      />
      <LabeledHandle
        label="Files"
        type="source"
        position="right"
        id="files"
        connectionCount="10"
        parameterType="FILENAMES_ARRAY"
      />
    </div>
  );
}
  