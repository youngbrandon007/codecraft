import { Injectable } from '@nestjs/common';
import { exec, ChildProcess } from 'node:child_process';
import * as process from 'node:process';

@Injectable()
export class VillagerService {
  initialize(): ChildProcess {
    const server = exec('java -jar server.jar', {cwd:"mc_server"}, (error, stdout, stderr) => {
      console.log(stdout)
    });

    return server;
  }

}
