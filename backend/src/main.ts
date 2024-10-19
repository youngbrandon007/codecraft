import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { exec } from 'node:child_process';
// import { VillagerModule } from './villager/villager.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  // const villager = await NestFactory.create(VillagerModule);
  const server = exec('java -jar mc_server/server.jar', {cwd:"mc_server"});


  await app.listen(3000);
}
bootstrap();
