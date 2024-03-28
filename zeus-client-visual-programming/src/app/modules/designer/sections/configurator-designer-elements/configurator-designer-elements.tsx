import React from 'react';

import './configurator-designer-elements.module.scss';
import {
  BlueprintComponentDto,
  ComponentDto,
  ElementType,
  ShapeType,
  WorkspaceDesignerDto
} from '../../../../../gen/api-client';
import { useStore } from '../../../../store';
import { ToolType } from '../../../../enums/tool-type.enum';
import ConfiguratorPreviewElements from '../../components/configurator-preview-elements/configurator-preview-elements';
import ConfiguratorActionsElementsVisibility
  from '../../components/configurator-actions-elements-visibility/configurator-actions-elements-visibility';
import ConfiguratorActionsElementsDelete
  from '../../components/configurator-actions-elements-delete/configurator-actions-elements-delete';
import ConfiguratorActions from '../../components/configurator-actions/configurator-actions';
import ConfiguratorRow from '../../../../components/configurator-row/configurator-row';
import ConfiguratorSectionHeader from '../../components/configurator-section-header/configurator-section-header';
import ConfiguratorColumn from '../../../../components/configurator-column/configurator-column';
import ConfiguratorElementsLayout from '../../components/configurator-elements-layout/configurator-elements-layout';
import TuneIcon from '@material-ui/icons/Tune';
import ConfiguratorElementsPosition
  from '../../components/configurator-elements-position/configurator-elements-position';
import ConfiguratorElementsDimensions
  from '../../components/configurator-elements-dimensions/configurator-elements-dimensions';
import SettingsIcon from '@material-ui/icons/Settings';
import ConfiguratorElementsFill from '../../components/configurator-elements-fill/configurator-elements-fill';
import ConfiguratorElementsBorderColor
  from '../../components/configurator-elements-border-color/configurator-elements-border-color';
import ConfiguratorElementsBorderWidth
  from '../../components/configurator-elements-border-width/configurator-elements-border-width';
import ConfiguratorElementsOpacity from '../../components/configurator-elements-opacity/configurator-elements-opacity';
import FormatPaintIcon from '@material-ui/icons/FormatPaint';
import ConfiguratorElementsShadowColor
  from '../../components/configurator-elements-shadow-color/configurator-elements-shadow-color';
import ConfiguratorElementsShadowProperties
  from '../../components/configurator-elements-shadow-properties/configurator-elements-shadow-properties';
import ConfiguratorElementsBorderRadius
  from '../../components/configurator-elements-border-radius/configurator-elements-border-radius';
import ConfiguratorElementsTextAlign
  from '../../components/configurator-elements-text-align/configurator-elements-text-align';
import TextSnippetIcon from '@material-ui/icons/TextSnippet';
import ConfiguratorElementsFontStyle
  from '../../components/configurator-elements-font-style/configurator-elements-font-style';
import ConfiguratorElementsTextDecoration
  from '../../components/configurator-elements-text-decoration/configurator-elements-text-decoration';
import ConfiguratorElementsTextTransform
  from '../../components/configurator-elements-text-transform/configurator-elements-text-transform';
import ConfiguratorElementsFontFamily
  from '../../components/configurator-elements-font-family/configurator-elements-font-family';
import ConfiguratorElementsFontSize
  from '../../components/configurator-elements-font-size/configurator-elements-font-size';
import { ComponentTreeNodeElement, ComponentUtils } from '../../component.utils';
import { useBlueprintComponentsWorkspace } from '../../data/blueprint-component-data.hooks';
import { AppUtils } from '../../../../app.utils';
import ConfiguratorActionsElementsMutationsReset
  from '../../components/configurator-actions-elements-mutations-reset/configurator-actions-elements-mutations-reset';
import AccountTree from '@material-ui/icons/AccountTree';
import ConfiguratorElementsThunder from '../../components/configurator-elements-thunder/configurator-elements-thunder';

export interface ConfiguratorDesignerComponentsProps {
  workspace: WorkspaceDesignerDto;
}

export function ConfiguratorDesignerElements(
  props: ConfiguratorDesignerComponentsProps
) {

  const activeTool = useStore(state => state.activeDesignerTool);
  const activeViewUuid = useStore(state => state.activeDesignerViewUuid);
  const selectedElementUuids = useStore(state => state.selectedComponentUuids);
  const stageBlueprintComponentProperties = useStore(state => state.designerStageBlueprintComponentProperties);
  const focusedComponentUuid = useStore(state => state.focusedComponentUuid);
  const { isLoading, isError, blueprintComponentDtos } = useBlueprintComponentsWorkspace(props.workspace.uuid);

  const view = (activeViewUuid !== null)
    ? props.workspace.views.find(view => view.uuid === activeViewUuid)
    : undefined;

  if ((view === undefined && !stageBlueprintComponentProperties.active) ||
    activeTool !== ToolType.POINTER ||
    selectedElementUuids.length === 0) {
    return null;
  }

  let blueprintComponent: BlueprintComponentDto | undefined = undefined;

  if (stageBlueprintComponentProperties.active) {
    blueprintComponent = (isLoading || isError)
      ? undefined
      : blueprintComponentDtos.find(blueprintComponent =>
        blueprintComponent.uuid === stageBlueprintComponentProperties.blueprintComponentUuid);
  }

  const selectedElements = ComponentUtils.flattenComponentTrees<ComponentDto | BlueprintComponentDto>(
    (!stageBlueprintComponentProperties.active)
      ? view.components
      : (blueprintComponent === undefined) ? [] : [blueprintComponent],
    (element) => selectedElementUuids.includes(element.element.uuid)
  );

  const shapesInSelection = ComponentUtils.getShapesOfElements<ComponentDto | BlueprintComponentDto>(selectedElements);

  const isTextSelection = shapesInSelection.every(shape => shape.type === ShapeType.Text);

  const focusedComponent: ComponentDto | undefined =
    (stageBlueprintComponentProperties.active || focusedComponentUuid === null)
      ? undefined
      : AppUtils.findInTrees<ComponentDto>(view.components, focusedComponentUuid).node;

  const isBlueprintComponentInstanceSelection =
    (focusedComponent !== undefined && focusedComponent['isBlueprintComponentInstance']) ||
    (focusedComponent === undefined && selectedElements.every(
      element => element.element['isBlueprintComponentInstance']
    ));

  return (
    <div>
      <ConfiguratorPreviewElements components={(!stageBlueprintComponentProperties.active)
        ? view.components
        : [blueprintComponent]}/>
      <ConfiguratorActions>
        <ConfiguratorActionsElementsVisibility
          shapes={shapesInSelection}
          elementUuids={selectedElementUuids}
          workspaceUuid={props.workspace.uuid}
        />
        {(!isBlueprintComponentInstanceSelection)
          ? null
          : <ConfiguratorActionsElementsMutationsReset workspaceUuid={props.workspace.uuid} />}
        {(focusedComponent !== undefined && focusedComponent.isBlueprintComponentInstance)
          ? null
          : <ConfiguratorActionsElementsDelete
            elementUuids={selectedElementUuids}
            workspaceUuid={props.workspace.uuid}
          />}
      </ConfiguratorActions>

      <ConfiguratorRow header={<ConfiguratorSectionHeader/>}>
        <ConfiguratorColumn style={{width: '100%'}}>
          <ConfiguratorElementsLayout
            elementUuids={selectedElementUuids}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow header={<ConfiguratorSectionHeader icon={TuneIcon} title={'General'}/>}>
        <ConfiguratorColumn>
          <ConfiguratorElementsPosition
            workspaceUuid={props.workspace.uuid}
            elementUuids={selectedElementUuids}
          />
        </ConfiguratorColumn>
        <ConfiguratorColumn>
          <ConfiguratorElementsDimensions
            workspaceUuid={props.workspace.uuid}
            elementUuids={selectedElementUuids}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow
        header={<ConfiguratorSectionHeader icon={TextSnippetIcon} title={'Text'}/>}
        visible={isTextSelection}
      >
        <ConfiguratorColumn>
        <ConfiguratorElementsFontFamily
          elementUuids={selectedElementUuids}
          shapes={shapesInSelection}
          workspaceUuid={props.workspace.uuid}
        />
        </ConfiguratorColumn>
        <ConfiguratorColumn>
          <ConfiguratorElementsFontSize
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow visible={isTextSelection}>
        <ConfiguratorColumn>
          <ConfiguratorElementsTextAlign
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow visible={isTextSelection} innerStyle={{justifyContent: 'flex-start'}}>
        <ConfiguratorColumn>
          <ConfiguratorElementsFontStyle
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
        <ConfiguratorColumn>
          <ConfiguratorElementsTextDecoration
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
        <ConfiguratorColumn>
          <ConfiguratorElementsTextTransform
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow
        header={<ConfiguratorSectionHeader icon={SettingsIcon} title={'Properties'}/>}
        innerStyle={{marginBottom: 0}}
      >
        <ConfiguratorColumn style={{width: '100%'}}>
          <ConfiguratorElementsFill
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow
        innerStyle={{marginBottom: 0}}>
        <ConfiguratorColumn>
          <ConfiguratorElementsBorderColor
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
        <ConfiguratorColumn>
          <ConfiguratorElementsBorderWidth
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow>
        <ConfiguratorColumn style={{width: '100%'}}>
          <ConfiguratorElementsOpacity
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow
        header={<ConfiguratorSectionHeader icon={FormatPaintIcon} title={'Styling'}/>}
        innerStyle={{marginBottom: 0}}
      >
        <ConfiguratorColumn>
          <ConfiguratorElementsShadowColor
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow>
        <ConfiguratorColumn style={{width: '100%'}}>
          <ConfiguratorElementsShadowProperties
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      <ConfiguratorRow innerStyle={{marginBottom: 0}}>
        <ConfiguratorColumn style={{width: '100%'}}>
          <ConfiguratorElementsBorderRadius
            elementUuids={selectedElementUuids}
            shapes={shapesInSelection}
            workspaceUuid={props.workspace.uuid}
          />
        </ConfiguratorColumn>
      </ConfiguratorRow>

      {(!stageBlueprintComponentProperties.active &&
        selectedElements.length === 1 &&
        selectedElements[0].type === ElementType.Component)
        ? <ConfiguratorRow header={<ConfiguratorSectionHeader icon={AccountTree} title={'Component Logic'} />}>
          <ConfiguratorElementsThunder element={selectedElements[0] as ComponentTreeNodeElement<ComponentDto>} />
      </ConfiguratorRow>
        : null}
    </div>
  );
}

export default ConfiguratorDesignerElements;
