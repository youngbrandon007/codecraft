import {
  ConnectedSocket,
  MessageBody, OnGatewayConnection,
  SubscribeMessage,
  WebSocketGateway,
  WebSocketServer,
} from "@nestjs/websockets";
import {Server, Socket} from 'socket.io';
import * as Y from "yjs"
import * as fs from "fs"
import { decode, encode } from './converter';
import { VillagerService } from '../villager/villager.service';
import { exec } from 'child_process';
import * as console from 'node:console';
const FILE_PATH = "data/run.py"

@WebSocketGateway({
  cors: {
    origin: '*',
  },
})
export class CodeGateway implements OnGatewayConnection {
  @WebSocketServer()
  server: Server;
  doc: Y.Doc;

  constructor(private villagerService:VillagerService) {
    this.doc = new Y.Doc();
    const text = this.doc.getText();

    fs.promises.readFile(FILE_PATH)
      .then((contents) => {
        text.insert(0, contents.toString());
      })
  }

  handleConnection(client: Socket) {
    const state = Y.encodeStateAsUpdate(this.doc)
    const str = encode(state);

    client.emit("update", str)
  }

  @SubscribeMessage('update')
  async update(
    @MessageBody() str: string,
    @ConnectedSocket() client: Socket,
  ) {
    client.broadcast.emit("update", str)

    const update = decode(str);

    Y.applyUpdate(this.doc, update)
  }

  @SubscribeMessage("run")
  async run() {
    this.log("Processing python...")

    const content = this.doc.getText().toString() as string;

    await this.villagerService.purgeFunctionFolder();

    fs.promises.writeFile(FILE_PATH, content).then(() => {
      const dataPacker = exec('java -jar ../fox/out/artifacts/naq_jar/naq.jar data/run.py mc_server/world/datapacks/codecraft/data/codecraft/function')

      // dataPacker.stderr.on('data', (data) => {console.log(data)})

      dataPacker.on("close", (code) => {
        this.log(`DataPacker closed with code ${code}`)

        this.villagerService.reload();
      })
    })
  }

  private log(msg:string): void {
    const timestamp = new Date();
    const formatted_timestamp = timestamp.toLocaleTimeString('en-US',{'hour12':false});

    console.log(`[${formatted_timestamp}] [Spider]: ${msg}`)
    console.log() // for prettier log formatting
  }
}