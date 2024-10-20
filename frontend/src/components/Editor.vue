<script setup lang="ts">
import {onMounted, ref, type Ref} from "vue";
import * as monaco from "@/lib/monaco";
import {MonacoBinding} from "y-monaco";
import * as Y from "yjs"
import {io, Socket} from "socket.io-client";
import {serverUrl} from "@/lib/connection";
import {decode, encode} from "@/lib/converter";

const editorElementRef: Ref<HTMLDivElement | null> = ref(null)

let socket: Socket | undefined;

onMounted( () => {
  socket = io(serverUrl)

  const doc = new Y.Doc()

  socket.on("connection", () => {
    console.log("Connected")
  })

  socket.on("disconnect", () => {
    console.log("Disconnected")
  })

  socket.on("update", (str: string) => {
    const update = decode(str)

    Y.applyUpdate(doc, update)
  })

  doc.on("update", (update) => {
    socket!.emit("update", encode(update))
  })

  setTimeout(async () => {
    const editorElement = editorElementRef.value!
    const monacoEditor = monaco.default.editor.create(editorElement, {
      automaticLayout: true,
      value: '',
      language: 'python',
      theme: 'vs-dark',
    })

    const text = doc.getText()
    new MonacoBinding(text, monacoEditor.getModel()!)
  }, 1)
})

function run() {
  socket!.emit("run")
}
</script>

<template>
  <div class="flex flex-col">
    <div class="flex flex-row p-2 gap-2 border-b-neutral-700 border-b">
      <div class="grow"></div>
      <button @click="run" type="button" class="rounded-md bg-green-600 px-2.5 py-1.5 text-sm font-semibold text-white shadow-sm hover:bg-green-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-green-600">Run</button>
    </div>
    <div ref="editorElementRef" class="w-full grow"></div>
  </div>
</template>

