import { Test, TestingModule } from '@nestjs/testing';
import { ShapeController } from './shape.controller';

describe('ShapeController', () => {
  let controller: ShapeController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [ShapeController],
    }).compile();

    controller = module.get<ShapeController>(ShapeController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
