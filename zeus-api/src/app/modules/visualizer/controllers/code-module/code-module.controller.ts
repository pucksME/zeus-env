import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseUUIDPipe,
  Post,
  Put,
  Req,
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
import { IdentifierLocation } from '../../../designer/enums/identifier-location.enum';
import { CodeModuleService } from '../../services/code-module/code-module.service';
import { CodeModuleDto } from '../../dtos/code-module.dto';
import { CreateCodeModuleDto } from '../../dtos/create-code-module.dto';
import { HasProjectPermission } from '../../../../guards/has-project-permission.decorator';
import { ProjectPermissionToken } from '../../../project/enums/project-permission-token.enum';
import { CodeModulesCategorizedDto } from '../../dtos/code-modules-categorized.dto';
import { UpdateCodeModuleDto } from '../../dtos/update-code-module.dto';
import { ErrorDto } from '../../dtos/error.dto';
import { CheckCodeDto } from '../../dtos/check-code.dto';

@ApiTags('Code Module')
@ApiBearerAuth()
@UseGuards(JwtAuthenticationGuard, PermissionGuard)
@Controller('code-module')
export class CodeModuleController {

  constructor(
    private readonly codeModuleService: CodeModuleService
  ) {
  }

  @ApiOperation({
    summary: 'Checks the provided code',
    description: 'Checks the provided code and returns potential errors'
  })
  @ApiOkResponse({
    description: 'The code module was checked successfully',
    type: [ErrorDto]
  })
  @Post('check')
  checkCode(@Body(ValidationPipe) checkCodeDto: CheckCodeDto): Promise<ErrorDto[]> {
    return this.codeModuleService.checkCode(checkCodeDto);
  }

  @ApiOperation({
    summary: 'Creates a new code module',
    description: 'Creates a new code module accessible within a project'
  })
  @ApiOkResponse({
    description: 'The code module was created successfully',
    type: CodeModuleDto
  })
  @ApiNotFoundResponse({ description: 'There was no project with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the project' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @HasProjectPermission({
    token: ProjectPermissionToken.WRITE,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'projectUuid',
    relations: ['codeModules']
  })
  @Post('project/:projectUuid')
  save(
    @Param('projectUuid', ParseUUIDPipe) projectUuid: string,
    @Body(ValidationPipe) createCodeModuleDto: CreateCodeModuleDto
  ): Promise<CodeModuleDto> {
    return this.codeModuleService.saveProject(projectUuid, createCodeModuleDto);
  }

  @ApiOperation({
    summary: 'Finds all code modules for a project',
    description: 'Finds all code modules for a project and categorizes them'
  })
  @ApiOkResponse({
    type: CodeModulesCategorizedDto,
    description: 'The categorized code modules were found successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no project with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the project' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  @HasProjectPermission({
    token: ProjectPermissionToken.READ,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'projectUuid'
  })
  @Get('categorized/:projectUuid')
  findProjectCodeModulesCategorized(
    @Req() req, @Param('projectUuid', ParseUUIDPipe) projectUuid: string
  ): Promise<CodeModulesCategorizedDto> {
    return this.codeModuleService.findCategorized();
  }

  @ApiOperation({
    summary: 'Finds all code modules of a project',
    description: 'Finds all code modules of a given project'
  })
  @ApiOkResponse({
    type: [CodeModuleDto],
    description: 'The code modules were found successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no project with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the project' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  @HasProjectPermission({
    token: ProjectPermissionToken.READ,
    extractFrom: IdentifierLocation.PARAMS,
    keyName: 'projectUuid'
  })
  @Get('project/:projectUuid')
  findProjectCodeModules(
    @Req() req, @Param('projectUuid', ParseUUIDPipe) projectUuid: string
  ): Promise<CodeModuleDto[]> {
    return this.codeModuleService.findProject();
  }

  @ApiOperation({
    summary: 'Finds all system code modules',
    description: 'Finds all system code modules'
  })
  @ApiOkResponse({
    type: [CodeModuleDto],
    description: 'The code modules were found successfully'
  })
  @Get('system')
  findSystemCodeModules(): Promise<CodeModuleDto[]> {
    return this.codeModuleService.findSystem();
  }

  @ApiOperation({
    summary: 'Finds a code module',
    description: 'Finds a code module by its uuid'
  })
  @ApiOkResponse({
    type: CodeModuleDto,
    description: 'The code module was found successfully'
  })
  @ApiNotFoundResponse({ description: 'There was no code module with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to see the code module' })
  @ApiBadRequestResponse({ description: 'The parameter projectUuid was not a valid UUID' })
  @Get(':codeModuleUuid')
  find(
    @Req() req, @Param('codeModuleUuid', ParseUUIDPipe) codeModuleUuid: string
  ): Promise<CodeModuleDto> {
    return this.codeModuleService.find(codeModuleUuid);
  }

  @ApiOperation({
    summary: 'Updates a code module',
    description: 'Updates a code module'
  })
  @ApiOkResponse({
    description: 'The code module was updated successfully',
    type: [ErrorDto]
  })
  @ApiNotFoundResponse({ description: 'There was no code module with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the code module' })
  @ApiBadRequestResponse({ description: 'The parameter codeModuleUuid was not a valid UUID' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Put(':codeModuleUuid')
  update(
    @Param('codeModuleUuid', ParseUUIDPipe) codeModuleUuid: string,
    @Body(ValidationPipe) updateCodeModuleDto: UpdateCodeModuleDto
  ): Promise<ErrorDto[]> {
    return this.codeModuleService.update(codeModuleUuid, updateCodeModuleDto);
  }

  @ApiOperation({
    summary: 'Deletes a code module',
    description: 'Deletes a code module'
  })
  @ApiOkResponse({ description: 'The code module was deleted successfully' })
  @ApiNotFoundResponse({ description: 'There was no code module with the given uuid' })
  @ApiForbiddenResponse({ description: 'The requesting user was not allowed to edit the code module' })
  @ApiBadRequestResponse({ description: 'The sent payload was not valid (invalid JSON syntax, failed validation pipeline, ...)' })
  @Delete(':codeModuleUuid')
  delete(
    @Param('codeModuleUuid', ParseUUIDPipe) codeModuleUuid: string
  ): Promise<void> {
    return this.codeModuleService.delete(codeModuleUuid);
  }

}
