import { Injectable } from '@nestjs/common';
import { DeleteResult, In, TreeRepository } from 'typeorm';
import { Component } from '../../entities/component.entity';
import { InjectRepository } from '@nestjs/typeorm';

@Injectable()
export class ComponentDataService {

  constructor(
    @InjectRepository(Component)
    private readonly componentRepository: TreeRepository<Component>
  ) {
  }

  save(componentEntity: Component): Promise<Component> {
    return this.componentRepository.save(componentEntity);
  }

  saveMany(componentEntities: Component[]): Promise<Component[]> {
    return this.componentRepository.save(componentEntities);
  }

  find(componentUuid: string, relations: string[] = []): Promise<Component | undefined> {
    return this.componentRepository.findOne({ uuid: componentUuid }, { relations });
  }

  findMany(componentUuids: string[], relations: string[] = []): Promise<Component[]> {
    return this.componentRepository.find({ where: { uuid: In(componentUuids) }, relations });
  }

  deleteMany(componentUuids: string[]): Promise<DeleteResult> {
    return this.componentRepository.delete(componentUuids);
  }

  findTrees(relations: string[] = []): Promise<Component[]> {
    return this.componentRepository.findTrees({relations});
  }

  findDescendants(componentEntity: Component, relations: string[] = []): Promise<Component> {
    return this.componentRepository.findDescendantsTree(componentEntity, {relations});
  }

  async findRoot(componentEntity: Component, relations: string[] = []): Promise<Component | undefined> {
    return (await this.componentRepository.findAncestors(
      componentEntity, {relations: [...relations, 'parent']}
    )).find(component => component.parent === null);
  }

}
