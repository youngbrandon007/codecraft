import {
  ConnectedSocket,
  MessageBody, OnGatewayConnection,
  SubscribeMessage,
  WebSocketGateway,
  WebSocketServer,
} from "@nestjs/websockets";
import {Server, Socket} from 'socket.io';
import * as Y from "yjs"
import {encode, decode} from 'uint8-to-base64';

@WebSocketGateway({
  cors: {
    origin: '*',
  },
})
export class CodeGateway implements OnGatewayConnection {
  @WebSocketServer()
  server: Server;
  doc: Y.Doc;

  constructor() {
    this.doc = new Y.Doc();
    const text = this.doc.getText();
    text.insert(0, "test")
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
}