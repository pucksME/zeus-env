import {
  ForbiddenException,
  Inject,
  Injectable,
  InternalServerErrorException,
  NotFoundException
} from '@nestjs/common';
import { REQUEST } from '@nestjs/core';
import { CodeModuleDataService } from '../../data/code-module-data/code-module-data.service';
import { CreateCodeModuleDto } from '../../dtos/create-code-module.dto';
import { CodeModuleDto } from '../../dtos/code-module.dto';
import { Project } from '../../../project/entities/project.entity';
import { RequestKeys } from '../../../../enums/request-keys.enum';
import { CodeModule } from '../../entities/code-module.entity';
import { CodeModuleUtils } from '../../code-module.utils';
import { UserProjectAssignment } from '../../../user/entities/user-project-assignment.entity';
import { CodeModulesCategorizedDto } from '../../dtos/code-modules-categorized.dto';
import { UpdateCodeModuleDto } from '../../dtos/update-code-module.dto';
import { CheckCodeDto } from '../../dtos/check-code.dto';
import { ErrorDto } from '../../dtos/error.dto';
import { ZeusCompilerApplicationApi } from '../../../../../gen/thunder-api-client';
import { AppUtils } from '../../../../app.utils';

@Injectable()
export class CodeModuleService {

  private thunderApplicationApi = new ZeusCompilerApplicationApi();

  constructor(
    @Inject(REQUEST)
    private readonly req,
    private readonly codeModuleDataService: CodeModuleDataService
  ) {
  }

  async checkCode(checkCodeDto: CheckCodeDto): Promise<ErrorDto[]> {
    return (await this.thunderApplicationApi.createCodeModules(
      [{ code: checkCodeDto.code }]
    )).data.flatMap(codeModuleDto => codeModuleDto.errors.map(CodeModuleUtils.buildErrorDto));
  }

  async saveProject(projectUuid: string, createCodeModuleDto: CreateCodeModuleDto): Promise<CodeModuleDto> {
    const userProjectAssignment: UserProjectAssignment = this.req[RequestKeys.USER_PROJECT_ASSIGNMENT];
    const project: Project = this.req[RequestKeys.PROJECT];

    if (userProjectAssignment === undefined) {
      throw new InternalServerErrorException('Could not save code module: user project assignment was not injected');
    }

    const user = userProjectAssignment.user;

    if (user === null) {
      throw new InternalServerErrorException('Could not save code module: user project assignment\'s user was not injected');
    }

    if (project === undefined) {
      throw new InternalServerErrorException('Could not save code module: project was not injected');
    }

    const codeModule = new CodeModule();
    codeModule.code = createCodeModuleDto.code;
    codeModule.global = false;
    codeModule.sorting = 0;
    codeModule.user = user;
    codeModule.project = project;

    project.codeModules.forEach(codeModule => codeModule.sorting++);
    await this.codeModuleDataService.saveMany(project.codeModules);

    return CodeModuleUtils.buildCodeModuleDto(await this.codeModuleDataService.save(codeModule));
  }

  async findCategorized(): Promise<CodeModulesCategorizedDto> {
    const project: Project = this.req[RequestKeys.PROJECT];

    if (project === undefined) {
      throw new InternalServerErrorException('Could not find categorized code modules: project was not injected');
    }

    return CodeModuleUtils.buildCodeModulesCategorizedDto(
      await this.codeModuleDataService.findManySystem(),
      await this.codeModuleDataService.findManyProject(project.uuid)
    );
  }

  async findSystem(): Promise<CodeModuleDto[]> {
    return (await this.codeModuleDataService.findManySystem()).map(CodeModuleUtils.buildCodeModuleDto);
  }

  async findProject(): Promise<CodeModuleDto[]> {
    const project: Project = this.req[RequestKeys.PROJECT];

    if (project === undefined) {
      throw new InternalServerErrorException('Could not find categorized code modules: project was not injected');
    }

    const codeModules = AppUtils.sort<CodeModule>(await this.codeModuleDataService.findManyProject(project.uuid));
    const createCodeModuleDtos = codeModules.map(codeModule => ({uuid: codeModule.uuid, code: codeModule.code}));
    return (await this.thunderApplicationApi.createCodeModules(createCodeModuleDtos)).data.flatMap(CodeModuleUtils.buildCodeModuleDtos);
  }

  async find(codeModuleUuid: string): Promise<CodeModuleDto> {
    const codeModule = await this.codeModuleDataService.find(codeModuleUuid, ['user']);

    if (codeModule === undefined) {
      throw new NotFoundException(`There was no code module with uuid ${codeModuleUuid}`);
    }

    if (codeModule.user !== null && this.req[RequestKeys.USER].sub !== codeModule.user.uuid) {
      throw new ForbiddenException();
    }

    return CodeModuleUtils.buildCodeModuleDto(await this.codeModuleDataService.find(codeModuleUuid));
  }

  async update(codeModuleUuid: string, updateCodeModuleDto: UpdateCodeModuleDto): Promise<ErrorDto[]> {
    const codeModule = await this.codeModuleDataService.find(codeModuleUuid, ['user']);

    if (codeModule === undefined) {
      throw new NotFoundException(`There was no code module with uuid ${codeModuleUuid}`);
    }

    if (codeModule.user === null || codeModule.user.uuid !== this.req[RequestKeys.USER].sub) {
      throw new ForbiddenException();
    }

    if (updateCodeModuleDto.code !== undefined) {
      codeModule.code = updateCodeModuleDto.code;
    }

    await this.codeModuleDataService.save(codeModule);

    const codeModulesDto = (await this.thunderApplicationApi.createCodeModules(
      [{code: updateCodeModuleDto.code}]
    )).data;

    return codeModulesDto.flatMap(codeModuleDto => codeModuleDto.errors.map(CodeModuleUtils.buildErrorDto));
  }

  async delete(codeModuleUuid: string): Promise<void> {
    const codeModule = await this.codeModuleDataService.find(codeModuleUuid, ['user']);

    if (codeModule === undefined) {
      throw new NotFoundException(`There was no code module with uuid ${codeModuleUuid}`);
    }

    if (codeModule.user === null || codeModule.user.uuid !== this.req[RequestKeys.USER].sub) {
      throw new ForbiddenException();
    }

    await this.codeModuleDataService.delete(codeModule.uuid);
  }
}
