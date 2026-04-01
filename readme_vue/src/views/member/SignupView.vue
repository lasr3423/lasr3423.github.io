<template>
    <div>
        <h2>회원가입</h2>

        <form @submit.prevent="handleSignup">
            <input v-model="form.email" type="email" placeholder="이메일" required /><br />
            <input v-model="form.password" type="password" placeholder="비밀번호" required /><br />
            <input v-model="pwconfirm" type="password" placeholder="비밀번호 확인" required /><br />
            <input v-model="form.name" type="text" placeholder="이름" required /><br />
            <input v-model="form.phone" type="tel" placeholder="전화번호" required /><br />
            <input v-model="form.address" type="text" placeholder="주소" required /><br />

            <label><input v-model="agreeAll" type="checkbox" />전체 동의</label><br />
            <label><input v-model="agreeTerms" type="checkbox" />[필수] 이용약관에 동의합니다.</label><br />
            <label><input v-model="agreePrivacy" type="checkbox" />[필수] 개인정보 수집 이용에 동의합니다.</label><br />
            <label><input v-model="form.marketingAgreed" type="checkbox" />[선택] 마케팅 정보 수신에 동의합니다. </label><br />

            <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
            <br />
            <router-link to="/">취소</router-link>
            <button type="submit" :disabled="loading">회원가입</button><br />
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

async function handleSignup() {
    errorMsg.value = "";

    if (form.password.length < 8) {
        errorMsg.value = "비밀번호는 8자 이상이어야 합니다.";
        return;
    }
    if (form.password !== pwconfirm.value) {
        errorMsg.value = "비밀번호가 일치하지 않습니다.";
        return;
    }
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
        errorMsg.value = e.response?.data?.message || "회원가입에 실패했습니다.";
    } finally {
        loading.value = false;
    }
}

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
