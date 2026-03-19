# 팀 Readme 코딩 컨벤션 가이드

## **1. HTML5 코드 스타일 가이드**

### **1.1. 기본 규칙**

- **들여쓰기**: 하드탭 사용
- **태그는 소문자 사용**: `<div>`, `<span>` 등
- **속성은 소문자 사용**: `<input type="text">`
- **속성 값에는 따옴표 사용**: `class="container"` (홑따옴표보다 큰따옴표 권장)
- **불필요한 닫는 태그 생략 금지**: `<br>`, `<img>`, `<input>` 등은 자체 종료 태그 사용

```html
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>HTML 스타일 가이드</title>
</head>
<body>
  <header>
    <h1>웹 개발 스타일 가이드</h1>
  </header>
  <main>
    <section>
      <p>HTML, CSS, JavaScript의 코딩 스타일 가이드</p>
    </section>
  </main>
  <footer>
    <p>&copy; 2025 Code Guide</p>
  </footer>
</body>
</html>
```

### **1.2. 문서 구조**

- `DOCTYPE` 선언 필수
- `<html>` 태그에 `lang` 속성 지정
- `<meta charset="UTF-8">` 설정
- `<title>` 태그는 필수
- `<meta name="viewport">`로 반응형 설정

### **1.3. 속성 정렬**

- 속성은 의미에 따라 순서 정리
1. 구조 관련 속성 (`id`, `class`, `data-*`)
2. 접근성 속성 (`aria-*`, `role`, `tabindex`)
3. 스타일 관련 속성 (`style`)
4. 기타 속성

```html
<input type="text"
  id="username"
  class="input-field"
  name="username"
  aria-label="사용자 이름"
  required
>
```

---

## **2. CSS3 코드 스타일 가이드**

### **2.1. 기본 규칙**

- **들여쓰기**: 하드탭 사용
- **소문자 사용**: `color`, `background-color` 등
- **세미콜론(;) 필수**: 모든 속성 선언 뒤에 `;`
- **중괄호 `{}` 다음 줄에서 시작**

```css
.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}
```

### **2.2. 선택자 규칙**

- **ID 사용 지양** (`#id`보다 `.class` 사용 권장)
- **CSS 네이밍은 BEM(Block-Element-Modifier) 방식 권장**

```css
/* BEM 방식 */
.button { /* Block */
  background-color: blue;
  color: white;
}

.button--large { /* Modifier */
  font-size: 20px;
}

.button__icon { /* Element */
  margin-right: 5px;
}
```

### **2.3. 색상 사용**

- HEX, RGB, HSL 사용 가능하지만 **HEX로 통일**
- 색상 변수 사용 (`CSS 변수 또는 SCSS 변수 권장`)

```css
:root {
  --primary-color: #3498db;
}

.button {
  background-color: var(--primary-color);
}
```

### **2.4. 미디어 쿼리 정렬**

- 스타일 정의 후 하단에 미디어 쿼리 정렬

```css
@media (max-width: 768px) {
  .container {
    width: 100%;
  }
}
```

---

## **3. JavaScript 코드 스타일 가이드**

### **3.1. 기본 규칙**

- **세미콜론(`;`) 사용**
- **하드탭 사용**
- **`const`, `let` 사용 (`var` 사용 금지)**
- **카멜 케이스 사용 (`camelCase`)**
- **화살표 함수 사용 (`()=>{}`)**

```jsx
const userName = "홍길동";

function greet(name) {
  return `안녕하세요, ${name}!`;
}

console.log(greet(userName));
```

### **3.2. 변수 및 함수 선언**

- **`const`로 선언 (변경 가능하면 `let` 사용)**
- **함수 이름은 동사로 시작**

```jsx
const isActive = true; // boolean 값
let userCount = 0; // 숫자 값

function getUserName() {
  return "홍길동";
}
```

### **3.3. 객체 및 배열 사용**

- **객체 키는 일관된 스타일 유지**
- **배열 사용 시 `map()`, `filter()`, `reduce()` 활용**

```jsx
const user = {
  id: 1,
  name: "홍길동",
  isActive: true,
};

const users = ["철수", "영희", "민수"];

users.forEach((user) => {
  console.log(user);
});
```

### **3.4. 조건문 및 반복문**

- **`===` 사용 (`==` 사용 지양)**
- **`if` 문 블록 `{}` 사용 (한 줄 코드라도 생략 금지)**

```jsx
if (user.isActive) {
  console.log("사용자 활성화 상태입니다.");
} else {
  console.log("사용자가 비활성화 상태입니다.");
}
```

### **3.5. 비동기 처리**

- **`async/await` 사용 (`then/catch` 대신)**
- **오류 처리는 `try/catch` 사용**

```jsx
async function fetchData() {
  try {
    const response = await fetch("https://api.example.com/data");
    const data = await response.json();
    console.log(data);
  } catch (error) {
    console.error("데이터 불러오기 실패:", error);
  }
}
```

### **3.6. 모듈화**

- **파일 분리 (`ES6 모듈` 사용)**
- **`export` `import` `default` 활용**

```jsx
// user.js
export function getUser() {
  return { id: 1, name: "홍길동" };
}

// main.js
import { getUser } from "./user.js";

const user = getUser();
console.log(user.name);
```

## **4. Java 코드 스타일 가이드**

Java의 코딩 컨벤션은 **Oracle의 Java Code Conventions** 및 **Google Java Style Guide**를 기반으로 정리합니다.

### **4.1. 기본 규칙**

✅ **파일 인코딩**: `UTF-8`

✅ **파일명**: 클래스명과 동일하게 설정 (`PascalCase` 적용)

✅ **세미콜론(`;`) 필수**

✅ **탭 대신 스페이스 4칸 사용**

✅ **클래스와 메서드 간 빈 줄 1줄 유지**

✅ **한 줄 길이는 100~120자 이내**

---

### **4.2. 네이밍 컨벤션**

| 요소 | 네이밍 규칙 | 예시 |
| --- | --- | --- |
| **클래스명** | `PascalCase` (대문자로 시작) | `UserService` |
| **메서드명** | `camelCase` (소문자로 시작) | `getUserInfo()` |
| **변수명** | `camelCase` (소문자로 시작) | `userName` |
| **상수명** | `UPPER_CASE` (언더스코어 사용) | `MAX_SIZE` |
| **패키지명** | 소문자, 도메인 기반 | `com.example.service` |
| **인터페이스명** | `PascalCase`, `-able` 또는 `I` 접두사 사용 가능 | `Runnable`, `IUserRepository` |

```java
public class UserService {
    private static final int MAX_RETRIES = 3;

    public String getUserInfo(String userId) {
        return "User Info: " + userId;
    }
}
```

---

### **4.3. 클래스 구조**

- **순서**: ① `필드` → ② `생성자` → ③ `메서드`
- **접근 제한자 순서**: `public` → `protected` → `private`

```java
public class User {
    private String name;
    private int age;

    // 생성자
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Getter
    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
```

---

### **4.4. 코드 스타일 세부 규칙**

✅ **중괄호 `{}` 는 항상 사용**

✅ **한 줄 `if` 문에도 중괄호 `{}` 포함**

✅ **`==` 대신 `equals()` 사용 (`String` 비교 시)**

✅ **제네릭 `<T>` 사용 시 공백 없음**

✅ **try-catch 블록에서 예외 메시지 출력 필수**

```java
if (user != null) {
    System.out.println(user.getName());
}

// String 비교
if ("admin".equals(userRole)) {
    System.out.println("관리자 계정");
}

// 예외 처리
try {
    int result = 10 / 0;
} catch (ArithmeticException e) {
    System.err.println("오류 발생: " + e.getMessage());
}
```

---

### **4.5. Stream API 및 람다 표현식**

✅ **`forEach()` 대신 `map()`, `filter()` 활용**

✅ **람다 표현식 사용 (`->`)**

```java
List<String> users = List.of("철수", "영희", "민수");

users.stream()
    .filter(name -> name.startsWith("철"))
    .forEach(System.out::println);
```

---

## **6. Spring Boot 3.x 코드 스타일 가이드**

### **6.1. 패키지 구조**

✅ **레이어드 아키텍처 적용**

✅ **패키지는 도메인 기준으로 나누기**

```
com.example.app
 ├── controller
 ├── service
 ├── repository
 ├── model
 ├── dto
 ├── config
```

---

### **6.2. Controller 코드 스타일**

✅ **`@RestController` 사용**

✅ **응답값은 `ResponseEntity<>` 활용**

✅ **DTO(Data Transfer Object) 사용하여 요청/응답 관리**

```java
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
```

---

### **6.3. Service 계층 코드 스타일**

✅ **비즈니스 로직은 `@Service` 에서 처리**

✅ **트랜잭션은 `@Transactional` 적용**

```java
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        return new UserDto(user);
    }
}
```

---

### **6.4. Repository 계층 코드 스타일**

✅ **Spring Data JPA의 `JpaRepository` 사용**

✅ **메서드 네이밍 컨벤션 준수 (`findBy...`, `existsBy...`)**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

---

### **6.5. DTO(Data Transfer Object) 활용**

✅ **엔티티 직접 반환 지양, DTO 사용**

```java
public record UserDto(Long id, String name, String email) {
    public UserDto(User user) {
        this(user.getId(), user.getName(), user.getEmail());
    }
}
```

## **11. Vue 3 코드 스타일 가이드**

Vue 3의 `<script setup>`은 Composition API를 기반으로 하며, 코드 가독성을 높이는 것이 중요합니다.

## **11.1. 기본 코드 스타일**

✅ **파일명은 PascalCase (`UserProfile.vue`)**

✅ **Props와 Emits는 `defineProps`, `defineEmits` 사용**

✅ **상태 관리는 `ref`, `reactive` 사용**

✅ **템플릿에서는 `v-bind`, `v-model` 적극 활용**

```jsx
<script setup>
import { ref } from "vue";

const count = ref(0);
const increment = () => count.value++;
</script>

<template>
  <div>
    <h1>Count: {{ count }}</h1>
    <button @click="increment">Increase</button>
  </div>
</template>
```

---

### **11.2. 네이밍 컨벤션**

| 요소 | 네이밍 규칙 | 예시 |
| --- | --- | --- |
| **컴포넌트명** | `PascalCase` | `UserProfile.vue` |
| **Props 변수** | `camelCase` | `userName` |
| **이벤트 핸들러** | `handle + 동작` | `handleSubmit()` |
| **상태 변수** | `ref()` 사용, `camelCase` | `const userName = ref("")` |
| **Vuex/Pinia 상태** | `camelCase` | `userStore` |

✅ **Props는 `defineProps()`로 정의**

```jsx
<script setup>
defineProps<{ name: string; age: number }>();
</script>
```

✅ **컴포넌트 스타일은 `<style scoped>` 사용**

```jsx
<style scoped>
button {
  background-color: blue;
  color: white;
}
</style>
```

---

### **11.3. 이벤트 핸들링 및 상태 관리**

✅ **`defineEmits()`로 이벤트 정의**

✅ **`v-model`을 활용한 양방향 바인딩**

```jsx
<script setup>
import { ref, defineEmits } from "vue";

const count = ref(0);
const emit = defineEmits(["updateCount"]);

const increment = () => {
  count.value++;
  emit("updateCount", count.value);
};
</script>

<template>
  <button @click="increment">Increase</button>
</template>
```

✅ **Vuex/Pinia 상태 관리는 `computed()`와 함께 사용**

```jsx
<script setup>
import { useCounterStore } from "@/stores/counter";
import { storeToRefs } from "pinia";

const counterStore = useCounterStore();
const { count } = storeToRefs(counterStore);
</script>
```

---

### **11.4. API 호출 및 비동기 처리**

✅ **`onMounted()` 내부에서 `async/await` 사용 가능**

✅ **`axios` 또는 `useFetch()`(Nuxt.js) 활용**

```jsx
<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";

const data = ref(null);

onMounted(async () => {
  const response = await axios.get("/api/data");
  data.value = response.data;
});
</script>
```