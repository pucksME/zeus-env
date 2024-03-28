import { SetMetadata } from '@nestjs/common';
import { MetadataKeys } from '../metadata-keys.enum';

// https://docs.nestjs.com/recipes/passport#enable-authentication-globally
export const Public = () => SetMetadata(MetadataKeys.PUBLIC, true);
