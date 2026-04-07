<template>
  <aside class="sidebar">

    <div class="sidebar-section">
      <div class="sidebar-title" @click="toggle('order')">
        주문/배송 <span>{{ open.order ? '▾' : '▸' }}</span>
      </div>
      <ul v-show="open.order">
        <li><router-link to="/mypage/order">주문 목록</router-link></li>
        <li><router-link to="/cart">장바구니</router-link></li>
        <li><router-link to="/mypage/payment">결제 내역</router-link></li>
      </ul>
    </div>

    <div class="sidebar-section">
      <div class="sidebar-title" @click="toggle('board')">
        게시글 <span>{{ open.board ? '▾' : '▸' }}</span>
      </div>
      <ul v-show="open.board">
        <li><router-link to="/notice">공지사항</router-link></li>
        <li><router-link to="/qna">QnA</router-link></li>
        <li><router-link to="/review">리뷰</router-link></li>
      </ul>
    </div>

    <div class="sidebar-section">
      <div class="sidebar-title" @click="toggle('myinfo')">
        회원 정보 <span>{{ open.myinfo ? '▾' : '▸' }}</span>
      </div>
      <ul v-show="open.myinfo">
        <li><router-link to="/mypage/edit">정보 수정</router-link></li>
        <li><router-link to="/mypage/password">비밀번호 변경</router-link></li>
        <li><router-link to="/mypage/withdraw">회원 탈퇴</router-link></li>
      </ul>
    </div>

  </aside>
</template>

<script setup>
import { reactive } from 'vue';

const open = reactive({
  order:  true,
  board:  false,
  myinfo: false,
});

function toggle(key) {
  open[key] = !open[key];
}
</script>
