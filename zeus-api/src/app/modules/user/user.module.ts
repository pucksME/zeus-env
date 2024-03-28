import { Module } from '@nestjs/common';
import { UserService } from './services/user.service';
import { UserDataService } from './data/user-data.service';
import { UserController } from './controllers/user.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { User } from './entities/user.entity';
import { UserProjectAssignment } from './entities/user-project-assignment.entity';
import { UserProjectAssignmentDataService } from './data/user-project-assignment-data/user-project-assignment-data.service';

@Module({
  imports: [TypeOrmModule.forFeature([
    User,
    UserProjectAssignment
  ])],
  providers: [UserService, UserDataService, UserProjectAssignmentDataService],
  exports: [
    UserDataService,
    UserProjectAssignmentDataService
  ],
  controllers: [UserController]
})
export class UserModule {
}
