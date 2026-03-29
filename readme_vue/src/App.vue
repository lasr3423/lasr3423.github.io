<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const store = useAuthStore();
const router = useRouter();
const logout = async () => {
  await store.logout();
  alert("로그아웃 되었습니다.");
  router.push("/");
}
onMounted(async () => {
  try {
    if(store.)
  }
})
</script>

<template>
</template>
