<template>
    <div>
        <h2>회원가입</h2>

        <form @submit.prevent="handleSignup">
            <input v-model="form.email" type="email" placeholder="이메일" required /><br />

            <input v-model="form.password" type="password" placeholder="비밀번호 (8자 이상)" required /><br />
            <input v-model="pwconfirm" type="password" placeholder="비밀번호 확인" required /><br />

            <input v-model="form.name" type="text" placeholder="이름" required /><br />

            <input
                v-model="form.phone"
                type="tel"
                placeholder="전화번호 (예: 01012345678)"
                required
            /><br />

            <input v-model="form.address" type="text" placeholder="주소" required /><br />

            <label><input v-model="agreeAll" type="checkbox" /> 전체 동의</label><br />
            <label><input v-model="agreeTerms" type="checkbox" /> [필수] 이용약관에 동의합니다.</label><br />
            <label><input v-model="agreePrivacy" type="checkbox" /> [필수] 개인정보 수집 이용에 동의합니다.</label><br />
            <label><input v-model="form.marketingAgreed" type="checkbox" /> [선택] 마케팅 정보 수신에 동의합니다.</label><br />

            <p v-if="errorMsg" style="color: red;">{{ errorMsg }}</p>
            <br />
            <router-link to="/">취소</router-link>
            &nbsp;
            <button type="submit" :disabled="loading">
                {{ loading ? '처리 중...' : '회원가입' }}
            </button>
        </form>

        <p>이미 계정이 있으신가요? <router-link to="/signin">로그인</router-link></p>
    </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { authApi } from "@/api/auth";

const router = useRouter();
const pwconfirm = ref("");
const loading = ref(false);
const errorMsg = ref("");
const agreeTerms = ref(false);
const agreePrivacy = ref(false);

const form = reactive({
    email: "",
    password: "",
    name: "",
    phone: "",
    address: "",
    marketingAgreed: false,
});

// 전화번호 형식 검사 (백엔드 @Pattern과 동일한 정규식)
// 010, 011, 016, 017, 018, 019 로 시작하는 10~11자리 숫자
const PHONE_REGEX = /^01[016789][0-9]{7,8}$/;

async function handleSignup() {
    errorMsg.value = "";

    // 비밀번호 길이
    if (form.password.length < 8) {
        errorMsg.value = "비밀번호는 8자 이상이어야 합니다.";
        return;
    }
    // 비밀번호 확인
    if (form.password !== pwconfirm.value) {
        errorMsg.value = "비밀번호가 일치하지 않습니다.";
        return;
    }
    // 전화번호 형식
    if (!PHONE_REGEX.test(form.phone)) {
        errorMsg.value = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)";
        return;
    }
    // 필수 약관 동의
    if (!agreeTerms.value || !agreePrivacy.value) {
        errorMsg.value = "필수 약관에 동의해주세요.";
        return;
    }

    try {
        loading.value = true;
        await authApi.signup(form);
        alert("회원가입이 완료되었습니다!");
        router.push("/signin");
    } catch (e) {
        // 서버에서 내려오는 에러 메시지 우선 표시
        // 서버 응답이 없으면 기본 메시지
        errorMsg.value =
            e.response?.data?.message ||
            e.response?.data ||
            "회원가입에 실패했습니다.";
    } finally {
        loading.value = false;
    }
}

// 전체 동의 체크박스 — 세 항목 모두 연동
const agreeAll = computed({
    get() {
        return agreeTerms.value && agreePrivacy.value && form.marketingAgreed;
    },
    set(value) {
        agreeTerms.value = value;
        agreePrivacy.value = value;
        form.marketingAgreed = value;
    },
});
</script>
