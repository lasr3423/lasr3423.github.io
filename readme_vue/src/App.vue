<template>
  <component :is="currentLayout">
    <router-view />
  </component>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import MainLayout from '@/layouts/MainLayout.vue';
import AdminLayout from '@/layouts/AdminLayout.vue';
import AuthLayout from '@/layouts/AuthLayout.vue';

const route = useRoute();

const currentLayout = computed(() => {
  if (route.meta.layout === 'admin') return AdminLayout;
  if (route.meta.layout === 'auth') return AuthLayout;
  return MainLayout;
});
</script>
