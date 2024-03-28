import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { ShapeMutation } from '../../entities/shape-mutation.entity';
import { In, Repository } from 'typeorm';

@Injectable()
export class ShapeMutationDataService {

  constructor(
    @InjectRepository(ShapeMutation)
    private readonly shapeMutationRepository: Repository<ShapeMutation>
  ) {
  }

  saveMany(shapeMutationsEntities: ShapeMutation[]): Promise<ShapeMutation[]> {
    return this.shapeMutationRepository.save(shapeMutationsEntities);
  }

  find(shapeMutationUuid: string, relations: string[] = []): Promise<ShapeMutation | undefined> {
    return this.shapeMutationRepository.findOne({ uuid: shapeMutationUuid }, {relations});
  }

  findMany(shapeMutationsUuids: string[], relations: string[] = []): Promise<ShapeMutation[]> {
    return this.shapeMutationRepository.find({where: {uuid: In(shapeMutationsUuids)}, relations});
  }
}
