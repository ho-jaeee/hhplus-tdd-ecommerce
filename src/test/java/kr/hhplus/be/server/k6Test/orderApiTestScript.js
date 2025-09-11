//실행 예제
// k6 run `
//    -e BASE_URL=http://localhost:8080 `
//    -e USERS=500 `
//    -e PRODUCT_MAX_ID=100 `
//    .\orderApiTestScript.js


import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { SharedArray } from 'k6/data';

// ====== ENV / CONFIG ======
const BASE = __ENV.BASE_URL || 'http://localhost:8080';

// 유저 범위: USER_MIN..USER_MAX (우선) 또는 1..USERS
const USERS = Number(__ENV.USERS || 500);
const USER_MIN = __ENV.USER_MIN ? Number(__ENV.USER_MIN) : null;
const USER_MAX = __ENV.USER_MAX ? Number(__ENV.USER_MAX) : null;

// 유저 선택 모드: random(기본) | vu (VU별 고정 userId)
const USER_MODE = (__ENV.USER_MODE || 'random').toLowerCase(); // 'random' | 'vu'

// HOT SKU
const HOT_SKU = (__ENV.HOT_SKU || '').trim().length > 0
    ? __ENV.HOT_SKU.split(',').map((s) => Number(s.trim()))
    : [];
const HOT_SKU_PROB = __ENV.HOT_SKU_PROB ? Math.min(Math.max(Number(__ENV.HOT_SKU_PROB), 0), 1) : (HOT_SKU.length > 0 ? 0.7 : 0);

// 랜덤 상품 ID 상한
const PRODUCT_MAX_ID = Number(__ENV.PRODUCT_MAX_ID || 5000);

// 주문 아이템 개수 범위
const ITEMS_MIN = Number(__ENV.ITEMS_MIN || 1);
const ITEMS_MAX = Number(__ENV.ITEMS_MAX || 1);

// 가격/수량 범위
const PRICE_MIN = Number(__ENV.PRICE_MIN || 1000);
const PRICE_MAX = Number(__ENV.PRICE_MAX || 1000);
const QTY_MIN = Number(__ENV.QTY_MIN || 1);
const QTY_MAX = Number(__ENV.QTY_MAX || 1);

// 쿠폰 (null이면 미적용)
const COUPON_ID = __ENV.COUPON_ID ? Number(__ENV.COUPON_ID) : null;

// think time
const THINK_MIN = __ENV.THINK_MIN ? Number(__ENV.THINK_MIN) : 0.2;
const THINK_MAX = __ENV.THINK_MAX ? Number(__ENV.THINK_MAX) : 0.6;

// 토큰 인증(선택): 파일 경로 전달 시 Authorization: Bearer <token>
const TOKENS_FILE = (__ENV.TOKENS_FILE || '').trim();
const TOKENS = new SharedArray('tokens', () => {
    if (!TOKENS_FILE) return [];
    // k6 내장 open() 사용 (파일은 스크립트와 같은 디렉토리 또는 상대 경로)
    // eslint-disable-next-line no-undef
    const raw = open(TOKENS_FILE);
    return raw.split('\n').map((l) => l.trim()).filter(Boolean);
});

// 디버그 로그 샘플 수
const DEBUG_SAMPLES = Number(__ENV.DEBUG_SAMPLES || 0);
let debugPrinted = 0;

// ====== METRICS ======
const orderLatency = new Trend('order_latency_ms');
const order_2xx = new Counter('order_2xx');
const order_4xx = new Counter('order_4xx');
const order_5xx = new Counter('order_5xx');
const errors = new Counter('errors_total');

// ====== SCENARIO (주문/결제 단독) ======
export const options = {
    scenarios: {
        order_checkout: {
            executor: 'ramping-arrival-rate',
            exec: 'scenario_order_checkout',
            startRate: 20, timeUnit: '1s',
            preAllocatedVUs: Number(__ENV.PRE_VUS || 100),
            maxVUs: Number(__ENV.MAX_VUS || 400),
            stages: [
                { duration: __ENV.STAGE1_DUR || '2m', target: Number(__ENV.STAGE1_TGT || 50) },  // warm-up
                { duration: __ENV.STAGE2_DUR || '8m', target: Number(__ENV.STAGE2_TGT || 50) },  // steady
                { duration: __ENV.STAGE3_DUR || '5m', target: Number(__ENV.STAGE3_TGT || 150) }, // ramp
                { duration: __ENV.STAGE4_DUR || '2m', target: Number(__ENV.STAGE4_TGT || 300) }, // spike
                { duration: __ENV.STAGE5_DUR || '3m', target: Number(__ENV.STAGE5_TGT || 0) },   // cool-down
            ],
            tags: { api: 'order_checkout' },
            gracefulStop: __ENV.GRACEFUL_STOP || '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'], // 에러율 < 1%
        'http_req_duration{api:order_checkout}': ['p(95)<300', 'p(99)<800'],
    },
    discardResponseBodies: false,
    insecureSkipTLSVerify: true,
};

// ====== HELPERS ======
function rnd(min, max) { return Math.random() * (max - min) + min; }
function irnd(min, max) { return Math.floor(rnd(min, max + 1)); }
function sleepRange(min, max) { sleep(rnd(min, max)); }

function pickUserId() {
    // 범위 결정
    const from = USER_MIN !== null ? USER_MIN : 1;
    const to = USER_MAX !== null ? USER_MAX : (USER_MIN !== null ? USER_MIN + USERS - 1 : USERS);

    if (USER_MODE === 'vu') {
        // VU 별 고정 유저: __VU는 1부터 시작
        // eslint-disable-next-line no-undef
        const span = to - from + 1;
        // eslint-disable-next-line no-undef
        const uid = from + ((__VU - 1) % span);
        return uid;
    }

    // random 모드 (기본)
    return irnd(from, to);
}

function pickProductId() {
    if (HOT_SKU.length > 0 && Math.random() < HOT_SKU_PROB) {
        return HOT_SKU[Math.floor(Math.random() * HOT_SKU.length)];
    }
    return irnd(1, PRODUCT_MAX_ID);
}

function makeItem() {
    const p = pickProductId();
    const price = irnd(PRICE_MIN, PRICE_MAX);
    const qty = irnd(QTY_MIN, QTY_MAX);
    return {
        productId: p,
        productName: `상품-${p}`, // 서버가 DB 기준으로 계산하면 무시돼도 OK
        pricePerUnit: price,
        quantity: qty,
    };
}

function makeItems() {
    const count = irnd(ITEMS_MIN, ITEMS_MAX);
    const arr = [];
    for (let i = 0; i < count; i++) arr.push(makeItem());
    return arr;
}

function pickAuthHeader() {
    if (TOKENS.length === 0) return {};
    const token = TOKENS[Math.floor(Math.random() * TOKENS.length)];
    return { Authorization: `Bearer ${token}` };
}

// ====== SCENARIO IMPL ======
export function scenario_order_checkout() {
    const u = pickUserId();
    const payload = {
        userId: u,
        couponId: COUPON_ID ?? null,
        items: makeItems(),
    };

    const headers = {
        'Content-Type': 'application/json',
        ...pickAuthHeader(),
    };

    const res = http.post(`${BASE}/orders`, JSON.stringify(payload), {
        headers,
        tags: { api: 'order_checkout', user: String(u) },
    });

    // 상태 분포 집계
    if (res.status >= 200 && res.status < 300) order_2xx.add(1);
    else if (res.status >= 400 && res.status < 500) order_4xx.add(1);
    else if (res.status >= 500) order_5xx.add(1);

    // 실패 샘플 로깅(최대 10건)
    if (res.status >= 400 && debugPrinted < Math.max(10, DEBUG_SAMPLES)) {
        console.log(`ORDER FAIL status=${res.status} userId=${u} body=${res.body}`);
        debugPrinted++;
    }

    // 디버그 샘플 로그 (성공/실패 무관, 최대 DEBUG_SAMPLES개)
    if (DEBUG_SAMPLES > 0 && debugPrinted < DEBUG_SAMPLES) {
        try {
            const firstItem = payload.items[0];
            console.log(`REQ userId=${u} prod=${firstItem?.productId} status=${res.status} t=${res.timings.duration}ms`);
            debugPrinted++;
        } catch (e) { /* noop */ }
    }

    orderLatency.add(res.timings.duration);
    const ok = check(res, { 'order: 201/2xx': (r) => r.status === 201 || r.status < 300 });
    if (!ok) errors.add(1);

    sleepRange(THINK_MIN, THINK_MAX);
}
