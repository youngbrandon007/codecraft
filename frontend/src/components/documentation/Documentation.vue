<script setup lang="ts">
import { LanguageDocumentation } from '@/lib/documenation';
</script>

<template>
  <div class="flex flex-col gap-2 text-wrap overflow-x-hidden overflow-y-auto p-2 divide-neutral-700 divide-y-2">
    <h1 class="text-3xl font-bold">Documentation</h1>
    <div v-for="item in LanguageDocumentation" class="py-2">
      <div class="flex flex-row gap-4 px-2">
        <h3 class="font-bold">{{item.name}}</h3>
        <div class="flex flex-col gap-2">
          <p>{{ item.description }}</p>

          <p v-if="item.parameters.length > 0" class="font-bold">Parameters</p>
          <p v-for="param in item.parameters">
            {{ param.name }}: {{ param.type }} - {{param.description}}
          </p>
        </div>


      </div>
    </div>
  </div>
</template>
