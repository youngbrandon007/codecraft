import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { VillagerModule } from './villager/villager.module';

@Module({
  imports: [VillagerModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
