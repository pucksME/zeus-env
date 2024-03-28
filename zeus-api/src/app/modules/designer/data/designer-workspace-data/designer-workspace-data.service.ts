import { Injectable } from '@nestjs/common';
import { DesignerWorkspace } from '../../entities/designer-workspace.entity';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';

@Injectable()
export class DesignerWorkspaceDataService {

  constructor(
    @InjectRepository(DesignerWorkspace)
    private readonly workspaceRepository: Repository<DesignerWorkspace>
  ) {
  }

  find(workspaceUuid: string, relations: string[] = []): Promise<DesignerWorkspace | undefined> {
    return this.workspaceRepository.findOne({ uuid: workspaceUuid }, { relations });
  }

  findMany(workspaceUuids: string[], relations: string[] = []): Promise<DesignerWorkspace[]> {
    return this.workspaceRepository.find({ where: { uuid: In(workspaceUuids) }, relations });
  }

}
