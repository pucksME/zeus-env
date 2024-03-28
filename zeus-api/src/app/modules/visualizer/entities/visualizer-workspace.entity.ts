import { Entity, OneToMany, OneToOne, PrimaryGeneratedColumn } from 'typeorm';
import { Component } from '../../designer/entities/component.entity';
import { CodeModuleInstance } from './code-module-instance.entity';
import { CodeModuleInstancesConnection } from './code-module-instances-connection.entity';

@Entity()
export class VisualizerWorkspace {
  @PrimaryGeneratedColumn('uuid')
  uuid: string;

  @OneToOne(
    type => Component,
    componentEntity => componentEntity.workspace
  )
  component: Component;

  @OneToMany(
    type => CodeModuleInstance,
    codeModuleInstanceEntity => codeModuleInstanceEntity.workspace,
    {cascade: true, onDelete: 'CASCADE'}
  )
  codeModuleInstances: CodeModuleInstance[];

  @OneToMany(
    type => CodeModuleInstancesConnection,
    codeModulesConnectionsEntity => codeModulesConnectionsEntity.visualizerWorkspace,
    {cascade: true}
  )
  codeModuleInstancesConnections: CodeModuleInstancesConnection[];
}
