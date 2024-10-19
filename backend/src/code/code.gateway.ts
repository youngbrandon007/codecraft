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
    console.log("RUNNING")

    const content = this.doc.getText().toString() as string;

    await fs.promises.writeFile(FILE_PATH, content)

    // stuff

    this.villagerService.reload();

  }
}