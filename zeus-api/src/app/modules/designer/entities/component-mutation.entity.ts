import { Column, Entity, ManyToOne, PrimaryGeneratedColumn } from 'typeorm';
import { Component } from './component.entity';
import { BlueprintComponent } from './blueprint-component.entity';

@Entity()
export class ComponentMutation {

  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @Column({ type: 'float', nullable: true })
  positionX: number | null;

  @Column({ type: 'float', nullable: true })
  positionY: number | null;

  @ManyToOne(
    type => BlueprintComponent,
    blueprintComponentEntity => blueprintComponentEntity.componentMutations,
    {onDelete: 'CASCADE'}
  )
  blueprintComponent: BlueprintComponent;

  @ManyToOne(
    type => Component,
    componentEntity => componentEntity.componentMutations,
    {onDelete: 'CASCADE'}
  )
  component: Component;

}
