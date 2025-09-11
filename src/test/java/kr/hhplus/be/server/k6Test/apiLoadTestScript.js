// 실행 예:
// k6 run `
//   --env BASE_URL=http://localhost:9205 `
// --env USERS=500 `
//   --env COUPON_ID=1001 `
//     .\apiLoadTestScript.js

import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// ====== ENV / CONFIG ======
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const USERS = Number(__ENV.USERS || 500);
const COUPON_ID = Number(__ENV.COUPON_ID || 1001);
const THINK_READ = { min: 0.1, max: 0.3 };
const THINK_WRITE = { min: 0.2, max: 0.6 };

// ====== METRICS ======
const couponLatency = new Trend('coupon_latency_ms');
const chargeLatency = new Trend('charge_latency_ms');
const rankingLatency = new Trend('ranking_latency_ms');
const errors = new Counter('errors_total');

// ====== SCENARIOS ======
export const options = {
    scenarios: {
        // 1) 포인트 충전
        points_charge: {
            executor: 'ramping-arrival-rate',
            exec: 'scenario_points_charge',
            startRate: 10, timeUnit: '1s',
            preAllocatedVUs: 50, maxVUs: 200,
            stages: [
                { duration: '5m', target: 20 },
                { duration: '10m', target: 20 },
                { duration: '10m', target: 60 },
                { duration: '3m', target: 120 },
                { duration: '5m', target: 0 },
            ],
            tags: { api: 'points_charge' },
        },

        // 2) 쿠폰 선착순 발급
        coupon_issue: {
            executor: 'ramping-arrival-rate',
            exec: 'scenario_coupon_issue',
            startRate: 30, timeUnit: '1s',
            preAllocatedVUs: 80, maxVUs: 300,
            stages: [
                { duration: '3m', target: 50 },
                { duration: '10m', target: 50 },
                { duration: '3m', target: 150 }, // 이벤트성 스파이크
                { duration: '7m', target: 50 },
                { duration: '5m', target: 0 },
            ],
            tags: { api: 'coupon_issue' },
        },

        // 3) 인기상품 조회(일간 랭킹)
        ranking_read: {
            executor: 'ramping-arrival-rate',
            exec: 'scenario_ranking_read',
            startRate: 80, timeUnit: '1s',
            preAllocatedVUs: 80, maxVUs: 300,
            stages: [
                { duration: '5m', target: 200 },
                { duration: '15m', target: 200 },
                { duration: '10m', target: 300 },
                { duration: '3m', target: 500 },
                { duration: '5m', target: 0 },
            ],
            tags: { api: 'ranking_read' },
        },
    },

    // ====== THRESHOLDS (SLO 얼라인) ======
    thresholds: {
        http_req_failed: ['rate<0.01'], // 전 시나리오 공통 에러율 < 1%
        'http_req_duration{api:ranking_read}': ['p(95)<200', 'p(99)<400'],
        'http_req_duration{api:coupon_issue}': ['p(95)<250', 'p(99)<600'],
        'http_req_duration{api:points_charge}': ['p(95)<250', 'p(99)<600'],
    },

    discardResponseBodies: false,
    insecureSkipTLSVerify: true,
};

// ====== HELPERS ======
function rnd(min, max) { return Math.random() * (max - min) + min; }
function sleepRange(r) { sleep(rnd(r.min, r.max)); }
function uid() { return Math.floor(Math.random() * USERS) + 1; }
function nowId(prefix) { return `${prefix}-${__VU}-${Date.now()}-${Math.floor(Math.random() * 1e6)}`; }

// ====== SCENARIO IMPLS ======

// 1) 포인트 충전 (PATCH /point/charge/{id} , body: { point })
export function scenario_points_charge() {
    const u = uid();
    const body = JSON.stringify({ point: 1000 });
    const res = http.patch(`${BASE}/point/charge/${u}`, body, {
        headers: { 'Content-Type': 'application/json' },
        tags: { api: 'points_charge' },
    });

    chargeLatency.add(res.timings.duration);
    const ok = check(res, { 'charge: 200': (r) => r.status === 200 });
    if (!ok) errors.add(1);

    sleepRange(THINK_WRITE);
}

// 2) 쿠폰 선착순 발급 (POST /api/new/coupons/{couponId}/issue?userId=&requestId=)
export function scenario_coupon_issue() {
    const u = uid();
    const rid = nowId('coupon');
    const url = `${BASE}/api/new/coupons/${COUPON_ID}/issue?userId=${u}&requestId=${encodeURIComponent(rid)}`;
    const res = http.post(url, null, { tags: { api: 'coupon_issue' } });

    couponLatency.add(res.timings.duration);
    // 정책에 따라 409(중복/품절) 허용
    const ok = check(res, { 'coupon: <400 or 409': (r) => r.status < 400 || r.status === 409 });
    if (!ok) errors.add(1);

    sleepRange(THINK_WRITE);
}

// 3) 인기상품 조회(일간) (GET /products/rank/today?n=20)
export function scenario_ranking_read() {
    const res = http.get(`${BASE}/products/rank/today?n=20`, {
        tags: { api: 'ranking_read' },
    });

    rankingLatency.add(res.timings.duration);
    const ok = check(res, { 'ranking: 200': (r) => r.status === 200 });
    if (!ok) errors.add(1);

    sleepRange(THINK_READ);
}
