import React from 'react';

import './workspace.module.scss';
import { DomElementIds } from '../../../constants';

export interface DesignerWorkspaceProps {
  children: React.ReactNode;
}

export function Workspace(props: DesignerWorkspaceProps) {
  return (
    <div
      id={DomElementIds.WORKSPACE_STAGE}
      style={{
        backgroundColor: '#eeeeee',
        height: '100%',
        overflow: 'hidden',
        width: '100%'
      }}>
      {props.children}
    </div>
  );
}

export default Workspace;
