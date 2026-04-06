<template>
  <div class="result-container">
    <div class="result-card">
      <div class="icon-check">✓</div>
      <h2 class="title">문의 삭제 완료</h2>
      <p class="msg">등록하신 1:1 문의가 정상적으로 삭제(처리)되었습니다.</p>
      <div class="actions">
        <button class="btn-primary" @click="$router.push('/qna')">
          QnA 목록으로
        </button>
        <button class="btn-outline" @click="$router.push('/mypage')">
          마이페이지 가기
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.result-container { display: flex; justify-content: center; padding: 100px 20px; background: #f8f9fa; min-height: 80vh; }
.result-card { background: #fff; padding: 40px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); text-align: center; max-width: 400px; width: 100%; }
.icon-check { width: 60px; height: 60px; background: #2c3e50; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 30px; margin: 0 auto 24px; }
.title { font-size: 1.5rem; font-weight: 700; color: #333; margin-bottom: 12px; }
.msg { color: #666; margin-bottom: 32px; line-height: 1.5; }
.actions { display: flex; flex-direction: column; gap: 12px; }
.btn-primary { background: #2c3e50; color: #fff; border: none; padding: 14px; border-radius: 8px; font-weight: 600; cursor: pointer; }
.btn-outline { background: #fff; color: #2c3e50; border: 1px solid #2c3e50; padding: 14px; border-radius: 8px; font-weight: 600; cursor: pointer; }
</style>