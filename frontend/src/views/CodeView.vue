<script setup lang="ts">
import Editor from "@/components/Editor.vue";
import { ref } from 'vue';
import Documentation from '@/components/documentation/Documentation.vue';

const showDocumentation = ref(false);
</script>

<template>
  <div class="w-screen h-screen flex flex-row">
    <Documentation v-if="showDocumentation" class="w-0 grow"></Documentation>
    <button @click="showDocumentation = !showDocumentation" class="h-full w-10 text-white flex border-x border-x-neutral-700 items-center justify-center font-bold cursor-pointer">
      <template v-if="showDocumentation">	&lt;</template>
      <template v-else>&gt;</template>
    </button>
    <Editor class="w-0 grow h-full"></Editor>
  </div>
</template>
