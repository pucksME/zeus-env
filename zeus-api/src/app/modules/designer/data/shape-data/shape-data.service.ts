import { Injectable } from '@nestjs/common';
import { DeleteResult, In, Repository } from 'typeorm';
import { Shape } from '../../entities/shape.entity';
import { InjectRepository } from '@nestjs/typeorm';

@Injectable()
export class ShapeDataService {

  constructor(
    @InjectRepository(Shape)
    private readonly shapeRepository: Repository<Shape>
  ) {
  }

  save(shapeEntity: Shape): Promise<Shape> {
    return this.shapeRepository.save(shapeEntity);
  }

  saveMany(shapeEntities: Shape[]): Promise<Shape[]> {
    return this.shapeRepository.save(shapeEntities);
  }

  find(shapeUuid: string, relations: string[] = []): Promise<Shape | undefined> {
    return this.shapeRepository.findOne({ uuid: shapeUuid }, { relations });
  }

  findMany(shapeUuids: string[], relations: string[] = []): Promise<Shape[]> {
    return this.shapeRepository.find({ where: { uuid: In(shapeUuids) }, relations });
  }

  deleteMany(shapeUuids: string[]): Promise<DeleteResult> {
    return this.shapeRepository.delete(shapeUuids);
  }

}
