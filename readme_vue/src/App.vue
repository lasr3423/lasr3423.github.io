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

</script>

<template>
  <div>
    <h1>쇼핑몰</h1>
    <nav>
      <router-link to="/">Home</router-link><br>
      <router-link to="/signin">로그인</router-link><br>
      <router-link to="/signup">회원가입</router-link><br>
    </nav>
  </div>
  <router-view />
  <footer>
  푸터
  </footer>
</template>
