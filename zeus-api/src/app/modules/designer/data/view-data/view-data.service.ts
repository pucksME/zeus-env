import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { View } from '../../entities/view.entity';
import { DeleteResult, Repository } from 'typeorm';

@Injectable()
export class ViewDataService {

  constructor(
    @InjectRepository(View)
    private readonly viewRepository: Repository<View>
  ) {
  }

  save(viewEntity: View): Promise<View> {
    return this.viewRepository.save(viewEntity);
  }

  saveMany(viewEntities: View[]): Promise<View[]> {
    return this.viewRepository.save(viewEntities);
  }

  find(viewUuid: string, relations: string[] = []): Promise<View | undefined> {
    return this.viewRepository.findOne({ uuid: viewUuid }, { relations });
  }

  delete(viewUuid: string): Promise<DeleteResult> {
    return this.viewRepository.delete(viewUuid);
  }

}
