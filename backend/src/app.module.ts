import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { VillagerModule } from './villager/villager.module';

import {CodeModule} from "./code/code.module";

@Module({
  imports: [VillagerModule, CodeModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
