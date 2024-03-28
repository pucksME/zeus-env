import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseUUIDPipe,
  Post,
  Put,
  UseGuards,
  ValidationPipe
} from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBearerAuth,
  ApiForbiddenResponse,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags
} from '@nestjs/swagger';
import {
  JwtAuthenticationGuard
} from '../../../authentication/guards/jwt-authentication-guard/jwt-authentication.guard';
import { PermissionGuard } from '../../../../guards/permission.guard';
import { CodeModuleInstanceService } from '../../services/code-module-instance/code-module-instance.service';
import { IdentifierLocation } from '../../../designer/enums/identifier-location.enum';
import { CodeModuleInstanceDto } from '../../dtos/code-module-instance.dto';
import { HasVisualizerPermission } from '../../../../guards/has-visualizer-permission.decorator';
import { VisualizerPermissionToken } from '../../enums/visualizer-permission-token.enum';
import { CreateCodeModuleInstanceDto } from '../../dtos/create-code-module-instance.dto';
import { PositionCodeModuleInstancesDto } from '../../dtos/position-code-module-instances.dto';
import { TranslateCodeModuleInstancesDto } from '../../dtos/translate-code-module-instances.dto';
import { DeleteCodeModuleInstancesDto } from '../../dtos/delete-code-module-instances.dto';
import { CreateCodeModuleInstanceConnectionDto } from '../../dtos/create-code-module-instance-connection.dto';
import { ErrorDto } from '../../dtos/error.dto';
import { CodeModuleInstanceConnectionDto } from '../../dtos/code-module-instance-connection.dto';
import { GetCodeModuleInstancesConnectionsDto } from '../../dtos/get-code-module-instances-connections.dto';

@ApiTags('Code Module Instance')
@ApiBearerAuth()
@UseGuards(JwtAuthenticationGuard, PermissionGuard)
@Controller('code-module-instance')
export class CodeModuleInstanceController {

  constructor(
    private readonly codeModuleInstanceService: CodeModuleInstanceService
  ) {
  }

  @ApiOperation({
    summary: 'Creates a new code module instance',
    description: 'Instantiates a code module within a component workspace'
  })
  @ApiOkResponse({
    description: 'The code module instance was created successfully',
    type: CodeModuleInstanceDto
  })
  @ApiNotFoundResponse({ description: 'There was no component with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The parameter componentUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasVisualizerPermission({
    token: VisualizerPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'componentUuid',
    relations: [
      'workspace',
      'workspace.codeModuleInstances',
      'workspace.codeModuleInstances.module'
    ]
  })
  @Post('component/:componentUuid')
  save(
    @Param('componentUuid', ParseUUIDPipe) componentUuid: string,
    @Body(ValidationPipe) createCodeModuleInstanceDto: CreateCodeModuleInstanceDto
  ): Promise<CodeModuleInstanceDto> {
    return this.codeModuleInstanceService.save(createCodeModuleInstanceDto);
  }

  @ApiOperation({
    summary: 'Updates the position of code module instances',
    description: 'Updates the position of multiple code module instances'
  })
  @ApiOkResponse({
    description: 'The code module instance positions were updated successfully',
    type: [CodeModuleInstanceDto]
  })
  @ApiNotFoundResponse({ description: 'There was no code module with the given uuid' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the code module' })
  // @ApiBadRequestResponse({ description: 'The parameter codeModuleUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('positionCodeModuleInstances')
  positionCodeModuleInstances(
    @Body(ValidationPipe) positionCodeModuleInstancesDto: PositionCodeModuleInstancesDto
  ): Promise<CodeModuleInstanceDto[]> {
    return this.codeModuleInstanceService.positionCodeModuleInstances(positionCodeModuleInstancesDto);
  }

  @ApiOperation({
    summary: 'Translates code module instances',
    description: 'Translates multiple code module instances'
  })
  @ApiOkResponse({
    description: 'The code module instances were translated successfully',
    type: [CodeModuleInstanceDto]
  })
  @ApiNotFoundResponse({ description: 'There was no code module with the given uuid' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the code module' })
  // @ApiBadRequestResponse({ description: 'The parameter codeModuleUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put('translateCodeModuleInstances')
  translateCodeModuleInstances(
    @Body(ValidationPipe) translateCodeModuleInstancesDto: TranslateCodeModuleInstancesDto
  ): Promise<CodeModuleInstanceDto[]> {
    return this.codeModuleInstanceService.translateCodeModuleInstances(translateCodeModuleInstancesDto);
  }

  @ApiOperation({
    summary: 'Deletes code module instances',
    description: 'Deletes multiple code module instances'
  })
  @ApiOkResponse({
    description: 'The code module instances were deleted successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no code module with the given uuid' })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the code module' })
  // @ApiBadRequestResponse({ description: 'The parameter codeModuleUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Delete('deleteCodeModuleInstances')
  deleteCodeModuleInstances(
    @Body(ValidationPipe) deleteCodeModuleInstancesDto: DeleteCodeModuleInstancesDto
  ): Promise<void> {
    return this.codeModuleInstanceService.deleteCodeModuleInstances(deleteCodeModuleInstancesDto);
  }

  @ApiOperation({
    summary: 'Creates a new code module instances connection',
    description: 'Creates a new connection between two code module instances'
  })
  @ApiOkResponse({
    description: 'The code module instances connection was created successfully',
    type: [ErrorDto]
  })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiForbiddenResponse({description: 'The connection did already exist, involves just one code module instance or the input is already connected'})
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Post(':componentUuid/codeModuleInstancesConnection')
  saveConnection(
    @Param('componentUuid', ParseUUIDPipe) componentUuid: string,
    @Body(ValidationPipe) createCodeModuleInstanceConnectionDto: CreateCodeModuleInstanceConnectionDto
  ): Promise<ErrorDto[]> {
    return this.codeModuleInstanceService.saveConnection(componentUuid, createCodeModuleInstanceConnectionDto);
  }

  @ApiOperation({
    summary: 'Finds code module instances connections of a component',
    description: 'Finds code module instances connections by their component\'s uuid'
  })
  @ApiOkResponse({
    description: 'The component\'s code module instances connections were found successfully',
    type: [CodeModuleInstanceConnectionDto]
  })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Get(':componentUuid/codeModuleInstancesConnections')
  getConnections(
    @Param('componentUuid', ParseUUIDPipe) componentUuid: string
  ): Promise<CodeModuleInstanceConnectionDto[]> {
    return this.codeModuleInstanceService.getConnections(componentUuid);
  }

  @ApiOperation({
    summary: 'Finds connections between code module instances',
    description: 'Finds connections between specific code module instances'
  })
  @ApiOkResponse({
    description: 'The code module instances connections were found successfully',
    type: [CodeModuleInstanceConnectionDto]
  })
  // @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Post('codeModuleInstancesConnections')
  getCodeModuleInstancesConnections(
    @Body(ValidationPipe) getCodeModuleInstancesConnectionsDto: GetCodeModuleInstancesConnectionsDto
  ): Promise<CodeModuleInstanceConnectionDto[]> {
    return this.codeModuleInstanceService.getCodeModuleInstancesConnections(getCodeModuleInstancesConnectionsDto);
  }

  @ApiOperation({
    summary: 'Deletes a code module instance connection',
    description: 'Deletes a connection between two code module instances'
  })
  @ApiOkResponse({
    description: 'The code module instance was deleted successfully'
  })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the workspace' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Delete('codeModuleInstanceConnection/:codeModuleInstanceConnectionUuid')
  deleteConnection(
    @Param('codeModuleInstanceConnectionUuid', ParseUUIDPipe) codeModuleInstanceConnectionUuid: string
  ): Promise<void> {
    return this.codeModuleInstanceService.deleteConnection(codeModuleInstanceConnectionUuid);
  }

}
