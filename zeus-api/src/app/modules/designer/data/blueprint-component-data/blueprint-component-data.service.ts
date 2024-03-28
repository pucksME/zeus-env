import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { BlueprintComponent } from '../../entities/blueprint-component.entity';
import { DeleteResult, TreeRepository } from 'typeorm';

@Injectable()
export class BlueprintComponentDataService {

  constructor(
    @InjectRepository(BlueprintComponent)
    private readonly blueprintComponentRepository: TreeRepository<BlueprintComponent>
  ) {
  }

  save(blueprintComponentEntity: BlueprintComponent): Promise<BlueprintComponent> {
    return this.blueprintComponentRepository.save(blueprintComponentEntity);
  }

  saveMany(blueprintComponentEntities: BlueprintComponent[]): Promise<BlueprintComponent[]> {
    return this.blueprintComponentRepository.save(blueprintComponentEntities);
  }

  find(blueprintComponentUuid: string, relations: string[] = []): Promise<BlueprintComponent | undefined> {
    return this.blueprintComponentRepository.findOne({uuid: blueprintComponentUuid}, {relations});
  }

  delete(blueprintComponentUuid: string): Promise<DeleteResult> {
    return this.blueprintComponentRepository.delete(blueprintComponentUuid);
  }

  deleteMany(blueprintComponentUuids: string[]): Promise<DeleteResult> {
    return this.blueprintComponentRepository.delete(blueprintComponentUuids);
  }

  findTrees(relations: string[] = []): Promise<BlueprintComponent[]> {
    return this.blueprintComponentRepository.findTrees({relations});
  }

  findDescendants(blueprintComponentEntity: BlueprintComponent, relations: string[] = []): Promise<BlueprintComponent> {
    return this.blueprintComponentRepository.findDescendantsTree(blueprintComponentEntity, {relations});
  }

}
