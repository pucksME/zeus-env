import { Column, Entity, ManyToOne, PrimaryGeneratedColumn } from 'typeorm';
import { Shape } from './shape.entity';
import { SpecificShapeProperties } from '../types/specific-shape-properties.type';
import { Component } from './component.entity';

@Entity()
export class ShapeMutation {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({ type: 'float', nullable: true })
  positionX: number | null;

  @Column({ type: 'float', nullable: true })
  positionY: number | null;

  @Column({ type: 'json', nullable: true })
  properties: Partial<SpecificShapeProperties> | null;

  @ManyToOne(
    type => Shape,
    shapeEntity => shapeEntity.mutations,
    {onDelete: 'CASCADE'}
  )
  shape: Shape;

  @ManyToOne(
    type => Component,
    componentEntity => componentEntity.shapeMutations,
    {onDelete: 'CASCADE'}
  )
  component: Component;

}
