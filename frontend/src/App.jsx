import React, { useState, useRef, useEffect } from 'react';
import { ReactFlowProvider } from '@xyflow/react';
import DockLayout from 'rc-dock';
import { SceneProvider } from './components/SceneContext';
import AppContent from './AppContent';
import FileExplorer from './components/FileExplorer';
import LoadingScreen from './components/LoadingScreen';
import ErrorScreen from './components/ErrorScreen';
import { useScene } from './components/SceneContext';
import NodeTypesPanel from './components/NodeTypesPanel';

// rc-dock layout with Flow Canvas (80%) and File Explorer (20%) split
const defaultLayout = {
  dockbox: {
    mode: 'horizontal',
    children: [
      {
        tabs: [
          {
            id: 'flowCanvas',
            title: 'Flow Canvas',
            content: <AppContent />,
            group: 'canvas',
          },
        ],
        size: 80, // 80% width
      },
      {
        size: 20,
        mode: 'vertical',
        children: [
          {
            size: 50,
            tabs: [
              {
                id: 'fileExplorer',
                title: 'File Explorer',
                content: (
                    <FileExplorer />
                ),
              },
            ],
          },
          {
            size: 50,
            tabs: [
              {
                id: 'nodeTypes',
                title: 'Node Types',
                content: (
                    <NodeTypesPanel />
                ),
              },
            ],
          },
        ],
      }
    ],
  },
};

// Component for loading and error handling
function AppWithSceneContext() {
  const { isSceneLoading, sceneError } = useScene();
  const layoutRef = useRef(null);

  // Persist and debug layout
  useEffect(() => {
    if (layoutRef.current) {
      const saved = localStorage.getItem('dockLayout');
      if (saved) {
        try {
          layoutRef.current.loadLayout(JSON.parse(saved));
        } catch (e) {
          console.error('Failed to load saved layout:', e);
        }
      }
      console.log('Initial rc-dock layout:', layoutRef.current.saveLayout());
    }
  }, []);

  const saveLayout = () => {
    if (layoutRef.current) {
      const saved = layoutRef.current.saveLayout();
      localStorage.setItem('dockLayout', JSON.stringify(saved));
      console.log('Saved rc-dock layout:', saved);
    }
  };

  if (isSceneLoading) {
    return <LoadingScreen message="Initializing your workspace..." />;
  }

  if (sceneError) {
    return <ErrorScreen message={sceneError} />;
  }

  return (
    <DockLayout
      ref={layoutRef}
      defaultLayout={defaultLayout}
      style={{
        position: 'absolute',
        left: 0,
        top: 0,
        right: 0,
        bottom: 0,
      }}
      groups={{
        canvas: { floatable: true, maximizable: true },
        explorer: { floatable: true, maximizable: true },
      }}
      dropMode="all"
      onLayoutChange={saveLayout}
    />
  );
}

export default function App() {
  const [error, setError] = useState(null);

  if (error) {
    return <ErrorScreen message={error} />;
  }

  return (
    <div style={{ height: '100vh', position: 'relative' }}>
      <ReactFlowProvider>
        <SceneProvider setError={setError}>
          <AppWithSceneContext />
        </SceneProvider>
      </ReactFlowProvider>
    </div>
  );
}