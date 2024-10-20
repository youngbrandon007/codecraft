import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { VillagerModule } from './villager/villager.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const villager = await NestFactory.create(VillagerModule);

  await app.listen(3000, "172.20.10.5");
}
bootstrap();
