import { Module } from '@nestjs/common';
import { VillagerService } from './villager.service';

@Module({
  providers: [VillagerService]
})
export class VillagerModule {
  constructor(private villagerService: VillagerService) {
    const server = villagerService.initialize();

    // server.stdout.on('data', (data) => {
    //   console.log(data);
    // })
  }
}
