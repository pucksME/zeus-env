import { ApiProperty } from "@nestjs/swagger";

export class VerifyCodeModuleDto {
  @ApiProperty()
  code: string;

  @ApiProperty()
  codeModuleName: string;
}