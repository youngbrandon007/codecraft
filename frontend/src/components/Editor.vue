<script setup lang="ts">
import {onMounted, ref, type Ref} from "vue";
import * as monaco from "@/lib/monaco";
import {MonacoBinding} from "y-monaco";
import * as Y from "yjs"
import {io} from "socket.io-client";
import {serverUrl} from "@/lib/connection";
import {encode, decode} from 'uint8-to-base64';

const editorElementRef: Ref<HTMLDivElement | null> = ref(null)

onMounted( () => {
  const socket = io(serverUrl)

  const doc = new Y.Doc()

  socket.on("connection", () => {
    console.log("Connected")
  })

  socket.on("disconnect", () => {
    console.log("Disconnected")
  })

  socket.on("update", (str) => {
    const update = decode(str)

    Y.applyUpdate(doc, update)
  })

  doc.on("update", (update) => {
    socket.emit("update", encode(update))
  })

  setTimeout(async () => {
    const editorElement = editorElementRef.value!
    const monacoEditor = monaco.default.editor.create(editorElement, {
      automaticLayout: true,
      value: '',
      language: 'python',
      theme: 'vs-dark'
    })

    const text = doc.getText()
    new MonacoBinding(text, monacoEditor.getModel()!)
  }, 1)
})
</script>

<template>
  <div ref="editorElementRef"></div>
</template>
