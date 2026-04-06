<template>
  <div class="admin-success">
    <h2>정상적으로 처리되었습니다.</h2>
    <p>요청하신 관리 업무(수정/삭제/등록)가 데이터베이스에 반영되었습니다.</p>
    <button @click="$router.back()">이전으로</button>
    <button @click="$router.push('/admin')">대시보드로</button>
  </div>
</template>