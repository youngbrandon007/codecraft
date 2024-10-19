import { Module } from '@nestjs/common';
import { VillagerModule } from './villager/villager.module';

import {CodeModule} from "./code/code.module";
import { ServeStaticModule } from '@nestjs/serve-static';

@Module({
  imports: [VillagerModule, CodeModule, ServeStaticModule.forRoot({
    rootPath: "./../frontend/dist",
  })],
  controllers: [],
  providers: [],
})
export class AppModule {}
