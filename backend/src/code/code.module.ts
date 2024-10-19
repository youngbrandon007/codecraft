import {Module} from "@nestjs/common";
import {CodeGateway} from "./code.gateway";
import { VillagerService } from '../villager/villager.service';
import { VillagerModule } from '../villager/villager.module';

@Module({
  imports: [VillagerModule],
  controllers: [],
  providers: [CodeGateway],
})
export class CodeModule {}
