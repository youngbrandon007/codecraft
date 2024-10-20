import { Injectable, OnApplicationBootstrap } from '@nestjs/common';
import { exec, ChildProcess } from 'node:child_process';
import * as console from 'node:console';
import * as fsExtra from 'fs-extra';

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

    purgeFunctionFolder() {
        fsExtra.emptyDirSync('mc_server/world/datapacks/codecraft/data/codecraft/function',
            (err) => {
                if (err) return this.log(`ERROR - ${err}`);
                this.log("Successfully purged old functions.");
            });
    }

    async copyLibs() {
        fsExtra.copy('data/lib','mc_server/world/datapacks/codecraft/data/codecraft/function',
            (err) => {
                if (err) return this.log(`ERROR - ${err}`);
                this.log("Successfully copied libs.");
            });
    }


    private log(msg:string): void {
        const timestamp = new Date();
        const formatted_timestamp = timestamp.toLocaleTimeString('en-US',{'hour12':false});

        console.log(`[${formatted_timestamp}] [Villager]: ${msg}`)
        console.log()
    }
}
