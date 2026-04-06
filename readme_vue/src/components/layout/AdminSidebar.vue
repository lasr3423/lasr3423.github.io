<template>
  <aside class="sidebar">

    <div class="sidebar-section">
      <div class="sidebar-title">
        <router-link to="/admin">대시보드</router-link>
      </div>
    </div>

    <div class="sidebar-section">
      <div class="sidebar-title" @click="toggle('adminOrder')">
        주문 관리 <span>{{ open.adminOrder ? '▾' : '▸' }}</span>
      </div>
      <ul v-show="open.adminOrder">
        <li><router-link to="/admin/order/list">주문 목록</router-link></li>
        <li><router-link to="/admin/delivery/list">배송 목록</router-link></li>
        <li><router-link to="/admin/category/list">카테고리 목록</router-link></li>
        <li><router-link to="/admin/payment/list">결제 목록</router-link></li>
      </ul>
    </div>

    <div class="sidebar-section">
      <div class="sidebar-title" @click="toggle('adminProduct')">
        상품 관리 <span>{{ open.adminProduct ? '▾' : '▸' }}</span>
      </div>
      <ul v-show="open.adminProduct">
        <li><router-link to="/admin/product/list">상품 목록</router-link></li>
        <li><router-link to="/admin/product/insert">상품 추가</router-link></li>
      </ul>
    </div>

    <div class="sidebar-section">
      <div class="sidebar-title" @click="toggle('adminBoard')">
        게시글 <span>{{ open.adminBoard ? '▾' : '▸' }}</span>
      </div>
      <ul v-show="open.adminBoard">
        <li><router-link to="/admin/notice/list">공지사항</router-link></li>
        <li><router-link to="/admin/qna/list">QnA 목록</router-link></li>
        <li><router-link to="/admin/review/list">리뷰 목록</router-link></li>
      </ul>
    </div>

    <div class="sidebar-section">
      <div class="sidebar-title" @click="toggle('adminMember')">
        회원 관리 <span>{{ open.adminMember ? '▾' : '▸' }}</span>
      </div>
      <ul v-show="open.adminMember">
        <li><router-link to="/admin/member/list">회원 목록</router-link></li>
        <li><router-link to="/admin/member/role">관리자 관리</router-link></li>
      </ul>
    </div>

  </aside>
</template>

<script setup>
import { reactive } from 'vue';

const open = reactive({
  adminOrder:   true,
  adminProduct: false,
  adminBoard:   false,
  adminMember:  false,
});

function toggle(key) {
  open[key] = !open[key];
}
</script>
