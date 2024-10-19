import { Module } from '@nestjs/common';
import { VillagerService } from './villager.service';

@Module({
  providers: [VillagerService],
  exports: [VillagerService]
})
export class VillagerModule {}
