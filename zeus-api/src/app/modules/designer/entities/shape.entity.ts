import { Column, Entity, ManyToOne, OneToMany, PrimaryGeneratedColumn } from 'typeorm';
import { ShapeType } from '../enums/shape-type.enum';
import { ShapeMutation } from './shape-mutation.entity';
import { Component } from './component.entity';
import { BlueprintComponent } from './blueprint-component.entity';
import { SpecificShapeProperties } from '../types/specific-shape-properties.type';

@Entity()
export class Shape {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({nullable: true})
  name: string;

  @Column('json')
  properties: SpecificShapeProperties;

  @Column({ type: 'float' })
  positionX: number;

  @Column({ type: 'float' })
  positionY: number;

  @Column({
    type: 'enum',
    enum: ShapeType
  })
  type: ShapeType;

  @Column()
  sorting: number;

  @ManyToOne(
    type => Component,
    componentEntity => componentEntity.shapes,
    { onDelete: 'CASCADE' }
  )
  component: Component;

  @ManyToOne(
    type => BlueprintComponent,
    blueprintComponentEntity => blueprintComponentEntity.shapes,
    { onDelete: 'CASCADE' }
  )
  blueprintComponent: BlueprintComponent;

  @OneToMany(
    type => ShapeMutation,
    shapeMutationsEntity => shapeMutationsEntity.shape,
    { cascade: true, onDelete: 'CASCADE' }
  )
  mutations: ShapeMutation[];

}
