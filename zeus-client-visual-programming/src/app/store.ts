import create from 'zustand';
import { ToolType } from './enums/tool-type.enum';
import { StageProperties } from './interfaces/stage-properties.interface';
import { StageDimensions } from './modules/designer/interfaces/stage-dimensions.interface';
import { BlueprintComponentDto, CodeModuleDto } from '../gen/api-client';
import { SelectionRectangleProperties } from './interfaces/selection-rectangle-properties.interface';
import { StorageKey } from './enums/storage-key.enum';
import { AppUtils } from './app.utils';
import { TextEditorState } from './modules/designer/interfaces/text-editor-state.interface';
import { TextStyleEditorState } from './modules/designer/interfaces/text-style-editor-state.interface';
import { CreateFormPreviewProperties } from './modules/designer/interfaces/create-form-preview-properties.interface';
import { SelectedElementsProperties } from './modules/designer/interfaces/selected-elements-properties.interface';
import spacing from '../assets/styling/spacing.json';
import { ThunderCodeEditorProperties } from './interfaces/thunder-code-editor-properties.interface';
import { CodeModuleInstancesConnection } from './interfaces/code-module-instances-connection.interface';
import { CodeModuleInstanceProperties } from './interfaces/code-module-instance-properties.interface';
import { SelectionTransformerProperties } from './interfaces/selection-transformer-properties.interface';

type BlueprintComponentStageProperties = StageProperties & {
  blueprintComponentUuid: string | null,
  initialContainerPosition: {
    x: number,
    y: number
  },
  active: boolean
};

interface Store {
  userSession: { token: string } | null,
  signIn: (token) => void,
  signOut: () => void,
  // designer stage
  designerStageProperties: StageProperties;
  setDesignerStageProperties: (designerStageProperties: StageProperties) => void;
  setDesignerStageDimensions: (dimensions: StageDimensions) => void;
  // designer stage blueprint component properties
  designerStageBlueprintComponentProperties: BlueprintComponentStageProperties;
  setDesignerStageBlueprintComponentProperties:
    (designerStageBlueprintComponentProperties: BlueprintComponentStageProperties) => void,
  setDesignerStageBlueprintComponentDimensions: (dimensions: StageDimensions) => void,
  resetDesignerStageBlueprintComponentProperties: () => void,
  // visualizer stage
  visualizerStageProperties: StageProperties;
  setVisualizerStageProperties: (visualizerStageProperties: StageProperties) => void;
  setVisualizerStageDimensions: (dimensions: StageDimensions) => void;
  // designer tools
  activeDesignerTool: ToolType;
  setActiveDesignerTool: (designerTool: ToolType) => void;
  // visualizer tools
  activeVisualizerTool: ToolType;
  setActiveVisualizerTool: (activeVisualizerTool: ToolType) => void;
  // visualizer selection transformer properties
  visualizerSelectionTransformerProperties: SelectionTransformerProperties;
  setVisualizerSelectionTransformerProperties: (visualizerSelectionTransformerProperties: SelectionTransformerProperties) => void;
  // code module instances properties
  codeModuleInstancesProperties: CodeModuleInstanceProperties[];
  setCodeModuleInstancesProperties: (codeModuleInstancesProperties: CodeModuleInstanceProperties[]) => void;
  // thunder editor
  thunderCodeEditorsProperties: ThunderCodeEditorProperties[];
  setThunderCodeEditorProperties: (thunderCodeEditorProperties: ThunderCodeEditorProperties) => void;
  codeModuleInstancesConnectionEditorState: CodeModuleInstancesConnection;
  setCodeModuleInstancesConnectionEditorState: (codeModuleInstancesConnection: CodeModuleInstancesConnection) => void;
  // Used to create or update new text elements
  textEditorState: TextEditorState;
  setTextEditorState: (textEditorState: TextEditorState) => void;
  resetTextEditorState: () => void;
  // Used to update the style (and dimensions) of text elements
  textStyleEditorState: TextStyleEditorState | null;
  setTextStyleEditorState: (textStyleEditorState: TextStyleEditorState) => void;
  resetTextStyleEditorState: () => void;
  activeDesignerViewUuid: string | null;
  transformActiveDesignerView: boolean;
  setTransformActiveDesignerView: (transformActiveDesignerView: boolean) => void;
  setActiveDesignerViewUuid: (viewUuid: string) => void;
  resetActiveDesignerView: () => void;
  // used to select designer elements and visual programming nodes
  selectionRectangleProperties: SelectionRectangleProperties;
  setSelectionRectangleProperties: (selectionRectangleProperties: SelectionRectangleProperties) => void;
  resetSelectionRectangleProperties: () => void;
  // selected components
  selectedComponentUuids: string[];
  setSelectedComponentUuids: (componentUuids: string[]) => void;
  refreshSelectedComponentUuids: () => void;
  selectedElementsProperties: SelectedElementsProperties,
  setSelectedElementsProperties: (selectedElementsProperties: SelectedElementsProperties) => void,
  // focus components
  focusedComponentUuid: string | null;
  focusComponent: (componentUuid: string) => void;
  endFocusComponent: () => void;
  // selected code module instances
  selectedCodeModuleInstanceUuids: string[];
  setSelectedCodeModuleInstanceUuids: (selectedCodeModuleInstanceUuids: string[]) => void;
  refreshSelectedCodeModuleInstanceUuids: () => void;
  resetDesignerState: () => void;
  configuratorIsLoading: boolean;
  setConfiguratorIsLoading: (configuratorIsLoading: boolean) => void;
  navigationIsLoading: boolean;
  setNavigationIsLoading: (navigationIsLoading: boolean) => void,
  // blueprint components drag and drop
  selectedBlueprintComponent: BlueprintComponentDto | null;
  setSelectedBlueprintComponent: (selectedBlueprintComponent: BlueprintComponentDto) => void;
  resetSelectedBlueprintComponent: () => void;
  // code modules drag and drop
  selectedCodeModule: CodeModuleDto | null;
  setSelectedCodeModule: (selectedCodeModule: CodeModuleDto) => void;
  resetSelectedCodeModule: () => void;
  createFormPreviewProperties: CreateFormPreviewProperties;
  setCreateFormPreviewProperties: (createFormPreviewProperties: CreateFormPreviewProperties) => void;
  resetCreateFormPreviewProperties: () => void;
}

const initialStoreData: Partial<Store> = {
  userSession: null,
  designerStageProperties: { height: 1, width: 1, x: 0, y: 0, scale: 1 },
  designerStageBlueprintComponentProperties: {
    blueprintComponentUuid: null,
    initialContainerPosition: {
      x: 0,
      y: 0
    },
    active: false,
    height: spacing.designerBlueprintComponentWorkspace.initialStageHeight,
    width: spacing.designerBlueprintComponentWorkspace.initialStageWidth,
    x: 0,
    y: 0,
    scale: 1
  },
  visualizerStageProperties: { height: 1, width: 1, x: 0, y: 0, scale: 1 },
  activeDesignerTool: ToolType.NAVIGATOR,
  activeVisualizerTool: ToolType.NAVIGATOR,
  visualizerSelectionTransformerProperties: {
    position: {x: 0, y: 0},
    dragOffset: {x: 0, y: 0}
  },
  codeModuleInstancesProperties: [],
  thunderCodeEditorsProperties: [],
  codeModuleInstancesConnectionEditorState: {input: null, output: null},
  textEditorState: {active: false, position: null, shape: null},
  textStyleEditorState: null,
  activeDesignerViewUuid: null,
  transformActiveDesignerView: false,
  selectionRectangleProperties: { height: 0, width: 0, positionX: 0, positionY: 0, active: false },
  selectedComponentUuids: [],
  selectedElementsProperties: {
    height: 1,
    width: 1,
    x: 0,
    y: 0,
    positionRelativeToView: {
      x: 0,
      y: 0
    },
    elements: []
  },
  focusedComponentUuid: null,
  selectedCodeModuleInstanceUuids: [],
  configuratorIsLoading: false,
  navigationIsLoading: false,
  selectedBlueprintComponent: null,
  selectedCodeModule: null,
  createFormPreviewProperties: {
    height: 0,
    width: 0,
    positionX: 0,
    positionY: 0,
    toolUsed: null
  }
};

export const useStore = create<Store>((set, get) => ({
  userSession: JSON.parse(localStorage.getItem(StorageKey.USER_SESSION)),
  signIn: (token) => {
    const userSession = { token };
    AppUtils.setApiToken(userSession.token);
    localStorage.setItem(StorageKey.USER_SESSION, JSON.stringify(userSession));
    set({ userSession });
  },
  signOut: () => {
    AppUtils.setApiToken('');
    localStorage.removeItem(StorageKey.USER_SESSION);
    set({ userSession: initialStoreData.userSession });
  },
  // designer stage properties
  designerStageProperties: initialStoreData.designerStageProperties,
  setDesignerStageProperties: (designerStageProperties: StageProperties) => set({ designerStageProperties }),
  setDesignerStageDimensions: (stageDimensions: StageDimensions) => set({
    designerStageProperties: { ...get().designerStageProperties, ...stageDimensions }
  }),
  // designer stage blueprint component properties
  designerStageBlueprintComponentProperties: initialStoreData.designerStageBlueprintComponentProperties,
  setDesignerStageBlueprintComponentProperties:
    (designerStageBlueprintComponentProperties: BlueprintComponentStageProperties) => set({
      designerStageBlueprintComponentProperties
    }),
  setDesignerStageBlueprintComponentDimensions: (stageDimensions: StageDimensions) => set({
    designerStageBlueprintComponentProperties: { ...get().designerStageBlueprintComponentProperties, ...stageDimensions }
  }),
  resetDesignerStageBlueprintComponentProperties: () => set({
    designerStageBlueprintComponentProperties: initialStoreData.designerStageBlueprintComponentProperties
  }),
  // visualizer stage properties
  visualizerStageProperties: initialStoreData.visualizerStageProperties,
  setVisualizerStageProperties: (visualizerStageProperties: StageProperties) => set({visualizerStageProperties}),
  setVisualizerStageDimensions: (stageDimensions: StageDimensions) => set({
    visualizerStageProperties: {...get().visualizerStageProperties, ...stageDimensions}
  }),
  // designer active view
  activeDesignerViewUuid: initialStoreData.activeDesignerViewUuid,
  transformActiveDesignerView: initialStoreData.transformActiveDesignerView,
  setTransformActiveDesignerView: (transformActiveDesignerView: boolean) => set({
    selectedComponentUuids: initialStoreData.selectedComponentUuids,
    focusedComponentUuid: initialStoreData.focusedComponentUuid,
    transformActiveDesignerView
  }),
  setActiveDesignerViewUuid: (activeDesignerViewUuid: string) => set({
    activeDesignerViewUuid,
    focusedComponentUuid: initialStoreData.focusedComponentUuid,
    selectedComponentUuids: initialStoreData.selectedComponentUuids,
    textEditorState: initialStoreData.textEditorState
  }),
  resetActiveDesignerView: () => set({
    activeDesignerViewUuid: initialStoreData.activeDesignerViewUuid,
    transformActiveDesignerView: initialStoreData.transformActiveDesignerView
  }),
  // designer tools
  activeDesignerTool: initialStoreData.activeDesignerTool,
  setActiveDesignerTool: (designerTool: ToolType) => set({
    activeDesignerTool: designerTool,
    transformActiveDesignerView: initialStoreData.transformActiveDesignerView
  }),
  // visualizer tools
  activeVisualizerTool: initialStoreData.activeVisualizerTool,
  setActiveVisualizerTool: (activeVisualizerTool: ToolType) => set({activeVisualizerTool}),
  visualizerSelectionTransformerProperties: initialStoreData.visualizerSelectionTransformerProperties,
  setVisualizerSelectionTransformerProperties: (visualizerSelectionTransformerProperties: SelectionTransformerProperties) => set({visualizerSelectionTransformerProperties}),
  codeModuleInstancesProperties: initialStoreData.codeModuleInstancesProperties,
  setCodeModuleInstancesProperties: (codeModuleInstancesProperties: CodeModuleInstanceProperties[]) => set({codeModuleInstancesProperties}),
  // thunder editor
  thunderCodeEditorsProperties: initialStoreData.thunderCodeEditorsProperties,
  setThunderCodeEditorProperties: (thunderCodeEditorProperties: ThunderCodeEditorProperties) => {
    const thunderCodeEditorsProperties = [...get().thunderCodeEditorsProperties];
    const thunderCodeEditorPropertiesIndex = thunderCodeEditorsProperties.findIndex(
      properties => properties.codeModuleUuid === thunderCodeEditorProperties.codeModuleUuid
    );

    if (thunderCodeEditorPropertiesIndex === -1) {
      thunderCodeEditorsProperties.push(thunderCodeEditorProperties);
      set({thunderCodeEditorsProperties});
      return;
    }

    thunderCodeEditorsProperties[thunderCodeEditorPropertiesIndex] = thunderCodeEditorProperties;
    set({thunderCodeEditorsProperties});
  },
  // code module instance connection editor
  codeModuleInstancesConnectionEditorState: initialStoreData.codeModuleInstancesConnectionEditorState,
  setCodeModuleInstancesConnectionEditorState: (codeModuleInstancesConnection: CodeModuleInstancesConnection) => set({codeModuleInstancesConnectionEditorState: codeModuleInstancesConnection}),
  textEditorState: initialStoreData.textEditorState,
  setTextEditorState: (textEditorState: TextEditorState) => set({textEditorState}),
  resetTextEditorState: () => set({textEditorState: { ...initialStoreData.textEditorState }}),
  textStyleEditorState: initialStoreData.textStyleEditorState,
  setTextStyleEditorState: (textStyleEditorState: TextStyleEditorState) => set({textStyleEditorState}),
  resetTextStyleEditorState: () => set({textStyleEditorState: initialStoreData.textStyleEditorState}),
  selectionRectangleProperties: initialStoreData.selectionRectangleProperties,
  setSelectionRectangleProperties: (selectionRectangleProperties: SelectionRectangleProperties) => set({ selectionRectangleProperties }),
  resetSelectionRectangleProperties: () => set({ selectionRectangleProperties: initialStoreData.selectionRectangleProperties }),
  // selected components
  selectedComponentUuids: initialStoreData.selectedComponentUuids,
  setSelectedComponentUuids: (selectedComponentUuids: string[]) => set({
    selectedComponentUuids,
    transformActiveDesignerView: false
  }),
  refreshSelectedComponentUuids: () => set({ selectedComponentUuids: [...get().selectedComponentUuids] }),
  selectedElementsProperties: initialStoreData.selectedElementsProperties,
  setSelectedElementsProperties: (selectedElementsProperties: SelectedElementsProperties) => set({selectedElementsProperties}),
  // focused component
  focusedComponentUuid: initialStoreData.focusedComponentUuid,
  focusComponent: (componentUuid: string) => set({
    selectedComponentUuids: initialStoreData.selectedComponentUuids,
    focusedComponentUuid: componentUuid,
    transformActiveDesignerView: false
  }),
  endFocusComponent: () => set({
    focusedComponentUuid: initialStoreData.focusedComponentUuid,
    selectedComponentUuids: initialStoreData.selectedComponentUuids
  }),
  // selected code module instances
  selectedCodeModuleInstanceUuids: initialStoreData.selectedCodeModuleInstanceUuids,
  setSelectedCodeModuleInstanceUuids: (selectedCodeModuleInstanceUuids: string[]) => set({selectedCodeModuleInstanceUuids}),
  refreshSelectedCodeModuleInstanceUuids: () => set({selectedCodeModuleInstanceUuids: [...get().selectedCodeModuleInstanceUuids]}),
  resetDesignerState: () => set({
    designerStageProperties: initialStoreData.designerStageProperties,
    designerStageBlueprintComponentProperties: initialStoreData.designerStageBlueprintComponentProperties,
    visualizerStageProperties: initialStoreData.visualizerStageProperties,
    activeDesignerViewUuid: initialStoreData.activeDesignerViewUuid,
    activeDesignerTool: initialStoreData.activeDesignerTool,
    selectionRectangleProperties: initialStoreData.selectionRectangleProperties,
    selectedComponentUuids: initialStoreData.selectedComponentUuids,
    focusedComponentUuid: initialStoreData.focusedComponentUuid,
    transformActiveDesignerView: initialStoreData.transformActiveDesignerView,
    textEditorState: initialStoreData.textEditorState
  }),
  configuratorIsLoading: initialStoreData.configuratorIsLoading,
  setConfiguratorIsLoading: (configuratorIsLoading) => set({configuratorIsLoading}),
  navigationIsLoading: initialStoreData.navigationIsLoading,
  setNavigationIsLoading: (navigationIsLoading: boolean) => set({navigationIsLoading}),
  // blueprint components drag and drop
  selectedBlueprintComponent: initialStoreData.selectedBlueprintComponent,
  setSelectedBlueprintComponent: (selectedBlueprintComponent: BlueprintComponentDto) => set({selectedBlueprintComponent}),
  resetSelectedBlueprintComponent: () => set({selectedBlueprintComponent: initialStoreData.selectedBlueprintComponent}),
  // code modules drag and drop
  selectedCodeModule: initialStoreData.selectedCodeModule,
  setSelectedCodeModule: (selectedCodeModule: CodeModuleDto) => set({selectedCodeModule}),
  resetSelectedCodeModule: () => set({selectedCodeModule: initialStoreData.selectedCodeModule}),
  // create form preview
  createFormPreviewProperties: initialStoreData.createFormPreviewProperties,
  setCreateFormPreviewProperties: (createFormPreviewProperties: CreateFormPreviewProperties) => set({createFormPreviewProperties}),
  resetCreateFormPreviewProperties: () => set({ createFormPreviewProperties: initialStoreData.createFormPreviewProperties })
}));
