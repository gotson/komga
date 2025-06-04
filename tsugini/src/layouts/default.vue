<template>
  <q-layout view="hHh Lpr lFf">
    <AppBar />

    <AppDrawer />

    <q-page-container>
      <q-page padding>
        <router-view />
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts"></script>
