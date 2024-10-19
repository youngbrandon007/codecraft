import { Injectable } from '@nestjs/common';
import { exec, ChildProcess } from 'node:child_process';

@Injectable()
export class VillagerService {
  private server:ChildProcess;

  constructor() {
    this.server = exec('java -jar server.jar', {cwd:"mc_server"},
      (error, stdout, stderr) => {});

    this.server.stdout.on('data', (data) => {
      console.log(data);
    })
  }

  private runCommand(cmd: string): void {
    this.server.stdin.write(cmd);
  }

  reload() {
    this.runCommand("/give Dannyx51 diamond 25\n"); 
  }
}
