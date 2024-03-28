import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { ComponentMutation } from '../../entities/component-mutation.entity';

@Injectable()
export class ComponentMutationDataService {

  constructor(
    @InjectRepository(ComponentMutation)
    private readonly componentMutationRepository: Repository<ComponentMutation>
  ) {
  }

  saveMany(componentMutationEntities: ComponentMutation[]): Promise<ComponentMutation[]> {
    return this.componentMutationRepository.save(componentMutationEntities);
  }

  find(componentMutationUuid: string, relations: string[] = []): Promise<ComponentMutation | undefined> {
    return this.componentMutationRepository.findOne({uuid: componentMutationUuid}, {relations});
  }

  findMany(componentMutationUuids: string[], relations: string[] = []): Promise<ComponentMutation[]> {
    return this.componentMutationRepository.find({where: {uuid: In(componentMutationUuids)}, relations});
  }

}
