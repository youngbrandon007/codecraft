import { Injectable, OnApplicationBootstrap } from '@nestjs/common';
import { exec, ChildProcess } from 'node:child_process';
import * as console from 'node:console';

@Injectable()
export class VillagerService implements OnApplicationBootstrap {
  private server:ChildProcess;

  private runCommand(cmd: string): void {
    this.server.stdin.write(cmd);
  }

  reload() {
    this.runCommand("reload\n");
  }

  onApplicationBootstrap(): any {
    this.server = exec('java -jar server.jar', { cwd: "mc_server" },
      (error, stdout, stderr) => {
    });

    this.server.stdout.on('data', (data) => {
      console.log(data);
    })

    this.server.stderr.on('data', (data) => {
      console.error(data);
    })
  }
}
