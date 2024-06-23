import { Project } from "./entities/project.entity";
import { ProjectDto } from "./dtos/project.dto";
import { ProjectGalleryDto } from "./dtos/project-gallery.dto";
import { ViewUtils } from "../designer/view.utils";
import { AppUtils } from "../../app.utils";
import { View } from "../designer/entities/view.entity";
import {
  ErrorDto,
  ExportBlueprintComponentDto,
  ExportBlueprintComponentReferenceDto,
  ExportCodeDto,
  ExportComponentDto,
  ExportComponentMutationDto,
  ExportedFileDto as CompilerExportedFileDto,
  ExportedProjectDto as CompilerExportedProjectDto,
  ExportElementDto,
  ExportElementDtoExportElementTypeEnum,
  ExportProjectDto,
  ExportProjectDtoExportTargetEnum,
  ExportShapeDto,
  ExportShapeDtoExportShapeTypeEnum,
  ExportShapeMutationDto,
  ExportShapePropertyDto,
  ExportShapePropertyDtoKeyEnum,
  ExportViewDto,
  TranslateProjectDtoExportTargetEnum
} from "../../../gen/thunder-api-client";
import { ExportTarget } from "./enums/export-target.enum";
import { BlueprintComponent } from "../designer/entities/blueprint-component.entity";
import { Shape } from "../designer/entities/shape.entity";
import { ShapeType } from "../designer/enums/shape-type.enum";
import {
  FontFamily,
  FontStyle,
  TextAlign,
  TextDecoration,
  TextTransform
} from "../designer/interfaces/shape-properties/text-properties.interface";
import { Component } from "../designer/entities/component.entity";
import { ComponentMutation } from "../designer/entities/component-mutation.entity";
import { ShapeMutation } from "../designer/entities/shape-mutation.entity";
import { VisualizerWorkspace } from "../visualizer/entities/visualizer-workspace.entity";
import { CodeModuleUtils } from "../visualizer/code-module.utils";
import { ExportedProjectDto } from "./dtos/exported-project.dto";
import { ExportedFileDto } from "./dtos/exported-file.dto";
import { Error } from "./interfaces/error.interface";
import { Archiver } from "archiver";
import { Monitor } from "./enums/monitor.enum";

export abstract class ProjectUtils {
  static buildProjectDto(projectEntity: Project): ProjectDto {
    return {
      uuid: projectEntity.uuid,
      name: projectEntity.name,
      description: projectEntity.description,
      workspaceUuid: projectEntity.designerWorkspace ? projectEntity.designerWorkspace.uuid : null
    };
  }

  static buildProjectGalleryDto(projectEntity: Project): ProjectGalleryDto {
    return {
      uuid: projectEntity.uuid,
      name: projectEntity.name,
      description: projectEntity.description,
      type: projectEntity.designerWorkspace ? projectEntity.designerWorkspace.type : null,
      view: (projectEntity.designerWorkspace &&
        projectEntity.designerWorkspace.views &&
        projectEntity.designerWorkspace.views.length !== 0)
        ? ViewUtils.buildViewDto(AppUtils.sort<View>(projectEntity.designerWorkspace.views)[0])
        : null
    };
  }

  static sortExportElements(exportElementDtos: ExportElementDto[]): ExportElementDto[] {
    return exportElementDtos.sort((exportElementA, exportElementB) => exportElementA.sorting - exportElementB.sorting);
  }

  static buildExportTarget(exportTarget: ExportTarget): ExportProjectDtoExportTargetEnum {
    switch (exportTarget) {
      case ExportTarget.REACT_TYPESCRIPT:
        return ExportProjectDtoExportTargetEnum.ReactTypescript;
    }
  }

  static buildExportBlueprintComponentDto(blueprintComponentEntity: BlueprintComponent): ExportBlueprintComponentDto {
    return {
      name: blueprintComponentEntity.name,
      positionX: ProjectUtils.buildNumericValue(blueprintComponentEntity.positionX),
      positionY: ProjectUtils.buildNumericValue(blueprintComponentEntity.positionY),
      sorting: blueprintComponentEntity.sorting,
      exportElementType: ExportElementDtoExportElementTypeEnum.BlueprintComponent,
      exportElementDtos: ProjectUtils.buildExportBlueprintElementDtos(
        blueprintComponentEntity.children,
        blueprintComponentEntity.shapes
      )
    };
  }

  static buildExportShapeType(shapeType: ShapeType): ExportShapeDtoExportShapeTypeEnum {
    switch (shapeType) {
      case ShapeType.RECTANGLE:
        return ExportShapeDtoExportShapeTypeEnum.Rectangle;
      case ShapeType.CIRCLE:
        return ExportShapeDtoExportShapeTypeEnum.Circle;
      case ShapeType.TEXT:
        return ExportShapeDtoExportShapeTypeEnum.Text;
    }
  }

  static buildShapePropertyKey(propertyName: string): ExportShapePropertyDtoKeyEnum {
    switch (propertyName) {
      case 'height':
        return ExportShapePropertyDtoKeyEnum.Height;
      case 'width':
        return ExportShapePropertyDtoKeyEnum.Width;
      case 'backgroundColorEnabled':
        return ExportShapePropertyDtoKeyEnum.BackgroundColorEnabled;
      case 'backgroundColor':
        return ExportShapePropertyDtoKeyEnum.BackgroundColor;
      case 'borderEnabled':
        return ExportShapePropertyDtoKeyEnum.BorderEnabled;
      case 'borderColor':
        return ExportShapePropertyDtoKeyEnum.BorderColor;
      case 'borderWidth':
        return ExportShapePropertyDtoKeyEnum.BorderWidth;
      case 'shadowEnabled':
        return ExportShapePropertyDtoKeyEnum.ShadowEnabled;
      case 'shadowColor':
        return ExportShapePropertyDtoKeyEnum.ShadowColor;
      case 'shadowX':
        return ExportShapePropertyDtoKeyEnum.ShadowX;
      case 'shadowY':
        return ExportShapePropertyDtoKeyEnum.ShadowY;
      case 'shadowBlur':
        return ExportShapePropertyDtoKeyEnum.ShadowBlur;
      case 'opacity':
        return ExportShapePropertyDtoKeyEnum.Opacity;
      case 'visible':
        return ExportShapePropertyDtoKeyEnum.Visible;
      case 'borderRadius':
        return ExportShapePropertyDtoKeyEnum.BorderRadius;
      case 'fontFamily':
        return ExportShapePropertyDtoKeyEnum.FontFamily;
      case 'fontSize':
        return ExportShapePropertyDtoKeyEnum.FontSize;
      case 'fontStyle':
        return ExportShapePropertyDtoKeyEnum.FontStyle;
      case 'text':
        return ExportShapePropertyDtoKeyEnum.Text;
      case 'textDecoration':
        return ExportShapePropertyDtoKeyEnum.TextDecoration;
      case 'textTransform':
        return ExportShapePropertyDtoKeyEnum.TextTransform;
      case 'textAlign':
        return ExportShapePropertyDtoKeyEnum.TextAlign;
    }
  }

  static buildFontFamilyShapePropertyValue(fontFamily: FontFamily) {
    switch (fontFamily) {
      case FontFamily.ARIAL:
        return 'arial';
    }
  }

  static buildFontStyleShapePropertyValue(fontStyle: FontStyle) {
    switch (fontStyle) {
      case FontStyle.NORMAL:
        return 'normal';
      case FontStyle.BOLD:
        return 'bold';
      case FontStyle.ITALIC:
        return 'italic';
    }
  }

  static buildTextDecorationShapePropertyValue(textDecoration: TextDecoration) {
    switch (textDecoration) {
      case TextDecoration.NONE:
        return 'none';
      case TextDecoration.UNDERLINE:
        return 'underline';
      case TextDecoration.STRIKE_THROUGH:
        return 'strike-through';
    }
  }

  static buildTextTransformShapePropertyValue(textTransform: TextTransform) {
    switch (textTransform) {
      case TextTransform.NONE:
        return 'none';
      case TextTransform.UPPERCASE:
        return 'uppercase';
    }
  }

  static buildTextAlignShapePropertyValue(textAlign: TextAlign) {
    switch (textAlign) {
      case TextAlign.LEFT:
        return 'left';
      case TextAlign.CENTER:
        return 'center';
      case TextAlign.RIGHT:
        return 'right';
    }
  }

  static buildNumericValue(value: number | null) {
    return (value !== null) ? Math.round(value) : null;
  }

  static buildShapePropertyValue(key: string, value: unknown) {
    switch (key) {
      case 'borderRadius':
        return `${value[0]},${value[1]},${value[2]},${value[3]}`;
      case 'fontFamily':
        return ProjectUtils.buildFontFamilyShapePropertyValue(value as FontFamily);
      case 'fontStyle':
        return ProjectUtils.buildFontStyleShapePropertyValue(value as FontStyle);
      case 'textDecoration':
        return ProjectUtils.buildTextDecorationShapePropertyValue(value as TextDecoration);
      case 'textTransform':
        return ProjectUtils.buildTextTransformShapePropertyValue(value as TextTransform);
      case 'textAlign':
        return ProjectUtils.buildTextAlignShapePropertyValue(value as TextAlign);
      case 'fontSize':
      case 'height':
      case 'width':
      case 'shadowX':
      case 'shadowY':
      case 'shadowBlur':
        return String(ProjectUtils.buildNumericValue(Number(value)));
      default:
        return String(value);
    }
  }

  static buildExportShapePropertyDtos(shapeProperties: unknown): ExportShapePropertyDto[] {
    return Object.keys(shapeProperties).map(key => ({
      key: ProjectUtils.buildShapePropertyKey(key),
      value: ProjectUtils.buildShapePropertyValue(key, shapeProperties[key])
    }));
  }

  static buildExportShapeDto(shapeEntity: Shape): ExportShapeDto {
    return {
      name: shapeEntity.name,
      positionX: ProjectUtils.buildNumericValue(shapeEntity.positionX),
      positionY: ProjectUtils.buildNumericValue(shapeEntity.positionY),
      sorting: shapeEntity.sorting,
      exportElementType: ExportElementDtoExportElementTypeEnum.Shape,
      exportShapeType: ProjectUtils.buildExportShapeType(shapeEntity.type),
      exportShapePropertyDtos: ProjectUtils.buildExportShapePropertyDtos(shapeEntity.properties)
    };
  }

  static buildExportBlueprintElementDtos(blueprintComponentEntities: BlueprintComponent[], shapeEntities: Shape[]): ExportElementDto[] {
    return ProjectUtils.sortExportElements([
      ...blueprintComponentEntities.map(ProjectUtils.buildExportBlueprintComponentDto),
      ...shapeEntities.map(ProjectUtils.buildExportShapeDto)
    ]);
  }

  static buildExportComponentMutationDto(componentMutationEntity: ComponentMutation): ExportComponentMutationDto {
    return {
      blueprintComponentName: componentMutationEntity.blueprintComponent.name,
      positionX: ProjectUtils.buildNumericValue(componentMutationEntity.positionX),
      positionY: ProjectUtils.buildNumericValue(componentMutationEntity.positionY)
    };
  }

  static buildExportShapeMutationDto(shapeMutationEntity: ShapeMutation): ExportShapeMutationDto {
    return {
      shapeName: shapeMutationEntity.shape.name,
      positionX: ProjectUtils.buildNumericValue(shapeMutationEntity.positionX),
      positionY: ProjectUtils.buildNumericValue(shapeMutationEntity.positionY),
      exportShapePropertyDtos: ProjectUtils.buildExportShapePropertyDtos(shapeMutationEntity.properties)
    }
  }

  static buildExportBlueprintComponentReferenceDto(
    blueprintComponentEntity: BlueprintComponent,
    componentMutations: ComponentMutation[],
    shapeMutations: ShapeMutation[]
  ): ExportBlueprintComponentReferenceDto {
    return {
      blueprintComponentName: blueprintComponentEntity.name,
      exportComponentMutationDtos: componentMutations.map(ProjectUtils.buildExportComponentMutationDto),
      exportShapeMutationDtos: shapeMutations.map(ProjectUtils.buildExportShapeMutationDto)
    };
  }

  static buildExportCodeDto(visualizerWorkspaceEntity: VisualizerWorkspace): ExportCodeDto {
    return {
      code: [
        ...visualizerWorkspaceEntity.codeModuleInstances.map(codeModuleInstance => codeModuleInstance.module.code),
        CodeModuleUtils.buildInstanceCodeModuleCode(visualizerWorkspaceEntity.codeModuleInstancesConnections.map(
          codeModuleInstancesConnection => ({
            input: {
              codeModuleName: codeModuleInstancesConnection.inputCodeModuleInstanceName,
              codeModulePortName: codeModuleInstancesConnection.inputCodeModuleInstancePortName
            },
            output: {
              codeModuleName: codeModuleInstancesConnection.outputCodeModuleInstanceName,
              codeModulePortName: codeModuleInstancesConnection.outputCodeModuleInstancePortName
            }
          })
        ))
    ].join('\n')
    };
  }

  static buildExportComponentDto(componentEntity: Component): ExportComponentDto {
    return {
      name: componentEntity.name,
      positionX: ProjectUtils.buildNumericValue(componentEntity.positionX),
      positionY: ProjectUtils.buildNumericValue(componentEntity.positionY),
      sorting: componentEntity.sorting,
      exportElementType: ExportElementDtoExportElementTypeEnum.Component,
      exportBlueprintComponentReferenceDto: (componentEntity.blueprintComponent !== null)
        ? ProjectUtils.buildExportBlueprintComponentReferenceDto(
          componentEntity.blueprintComponent,
          componentEntity.componentMutations,
          componentEntity.shapeMutations
        )
        : null,
      exportCodeDto: (componentEntity.workspace !== null)
        ? ProjectUtils.buildExportCodeDto(componentEntity.workspace)
        : null,
      exportElementDtos: ProjectUtils.buildExportElementDtos(componentEntity.children, componentEntity.shapes)
    };
  }

  static buildExportElementDtos(componentEntities: Component[], shapeEntities: Shape[]): ExportElementDto[] {
    return ProjectUtils.sortExportElements([
      ...componentEntities.map(ProjectUtils.buildExportComponentDto),
      ...shapeEntities.map(ProjectUtils.buildExportShapeDto)
    ]);
  }

  static buildExportViewDto(viewEntity: View): ExportViewDto {
    return {
      name: viewEntity.name,
      height: viewEntity.height,
      width: viewEntity.width,
      positionX: ProjectUtils.buildNumericValue(viewEntity.positionX),
      positionY: ProjectUtils.buildNumericValue(viewEntity.positionY),
      isRoot: viewEntity.isRoot,
      exportElementDtos: viewEntity.components.map(ProjectUtils.buildExportComponentDto)
    };
  }

  static buildExportProjectDto(projectEntity: Project, exportTarget: ExportTarget): ExportProjectDto {
    return {
      name: projectEntity.name,
      exportElementDtos: projectEntity.designerWorkspace.blueprintComponents.map(
        ProjectUtils.buildExportBlueprintComponentDto
      ),
      exportViewDtos: projectEntity.designerWorkspace.views.map(ProjectUtils.buildExportViewDto),
      exportTarget: ProjectUtils.buildExportTarget(exportTarget)
    }
  }

  static buildExportedFileDto(compilerExportedFileDto: CompilerExportedFileDto): ExportedFileDto {
    return {
      code: compilerExportedFileDto.code,
      filename: compilerExportedFileDto.filename
    };
  }

  static buildExportedProjectDto(
    projectUuid: string,
    exportTarget: ExportTarget,
    compilerExportedProjectDto: CompilerExportedProjectDto
  ): ExportedProjectDto {
    return {
      uuid: projectUuid,
      exportedFileDtos: (compilerExportedProjectDto.exportedClientDtos.length == 0)
        ? []
        : compilerExportedProjectDto.exportedClientDtos[0].exportedFileDtos.map(ProjectUtils.buildExportedFileDto),
      exportTarget,
      errors: compilerExportedProjectDto.errors.map(CodeModuleUtils.buildErrorDto)
    };
  }

  static translateError(error: Error | ErrorDto): string {
    return error.message;
  }

  static buildZeusCompilerExportTarget(exportTarget: ExportTarget): TranslateProjectDtoExportTargetEnum {
    switch (exportTarget) {
      case ExportTarget.REACT_TYPESCRIPT:
        return TranslateProjectDtoExportTargetEnum.ReactTypescript
      case ExportTarget.EXPRESS_TYPESCRIPT:
        return TranslateProjectDtoExportTargetEnum.ReactTypescript
    }
  }

  static buildExportProjectFrameworkFiles(
    archiver: Archiver,
    frameworkPath: string,
    filePaths: string[],
    archivePath: string = ''
  ): Archiver {
    for (const filePath of filePaths) {
      archiver.file(frameworkPath + filePath, {name: archivePath + filePath});
    }
    return archiver;
  }

  static buildExportProjectFramework(
    archiver: Archiver,
    exportTarget: ExportTarget,
    archivePath: string = ''
  ): Archiver {
    switch (exportTarget) {
      case ExportTarget.REACT_TYPESCRIPT:
        return ProjectUtils.buildExportProjectFrameworkFiles(
          archiver,
          './frameworks/framework-react-typescript/',
          [
            'src/main.tsx',
            'src/vite-env.d.ts',
            'eslintrc.cjs',
            'index.html',
            'package.json',
            'README.md',
            'source.md',
            'tsconfig.json',
            'tsconfig.node.json',
            'vite.config.ts'
          ],
          archivePath
        );
      case ExportTarget.EXPRESS_TYPESCRIPT:
        return ProjectUtils.buildExportProjectFrameworkFiles(
          archiver,
          './frameworks/framework-express-typescript/',
          [
            'index.ts',
            'package.json',
            'tsconfig.json'
          ],
          archivePath
        )
    }
  }

  static getExportedFilePath(exportTarget: ExportTarget) {
    switch (exportTarget) {
      case ExportTarget.REACT_TYPESCRIPT:
        return 'src/';
    }
  }

  static buildExportProjectMonitor(archiver: Archiver, monitor: Monitor, archivePath: string = '') {
    switch (monitor) {
      case Monitor.BOOTS:
        return ProjectUtils.buildExportProjectFrameworkFiles(
          archiver,
          './monitors/boots/',
          [
            'boots.py'
          ],
          archivePath
        )
    }
  }

  static buildExportProjectMonitorAdapter(archiver: Archiver, monitor: Monitor, archivePath: string = '') {
    switch (monitor) {
      case Monitor.BOOTS:
        return ProjectUtils.buildExportProjectFrameworkFiles(
          archiver,
          './frameworks/framework-express-typescript/monitor-adapters/',
          [
            'boots-monitor.adapter.ts'
          ],
          archivePath
        )
    }
  }

  static buildExportProjectErrors(archiver: Archiver, errors: (Error | ErrorDto)[]): Archiver {
    if (errors.length === 0) {
      return archiver;
    }

    archiver.append(errors.map(ProjectUtils.translateError).join('\n'), {name: 'errors.txt'});
    return archiver;
  }
}
