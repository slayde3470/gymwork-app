# 홍겸 님 플랜 — 종목별 '주차 처방' (루틴에 그 종목을 넣으면 이번 주 처방이 채워지게)
import json, datetime as dt
START = dt.date(2026, 9, 28)
def mon(n): return START + dt.timedelta(days=7 * (n - 1))
def r25(x): return round(x / 2.5) * 2.5
def r5(x): return max(5, int(round(x / 5.0)) * 5)
def ri(x, lo=1): return max(lo, int(round(x)))
def S(reps, w=0.0, rest=90):
    return {"세트값": [{"w": w if not isinstance(w, list) else w[i], "r": r} for i, r in enumerate(reps)],
            "휴식값": [rest] * len(reps)}
def sess(name, reps, w=0.0, rest=90, kind="보통", target=None, memo=None):
    d = {"세션": name, **S(reps, w, rest), "종류": kind}
    if target is not None: d["목표"] = target
    if memo: d["메모"] = memo
    return d
def geo(first, last, a, b, dl, rnd):
    ws = [n for n in range(first, last + 1) if not dl(n)]
    out, cur = {}, None
    for i, n in enumerate(ws): out[n] = rnd(a * (b / a) ** (i / (len(ws) - 1)))
    for n in range(first, last + 1):
        cur = out.get(n, cur); out[n] = cur
    return out

# ── 1. 맨몸 스쿼트 20 → 500 (2027-06-30 수) · 40주 · 주 2회 ─────────
SQ_END = 40
sq_dl = lambda n: n % 4 == 0 and n < SQ_END
sq_ts = lambda n: (n % 8 == 0 and n < SQ_END) or n == SQ_END
SQL = geo(1, SQ_END - 1, 20, 440, sq_dl, r5)
def sq_week(n):
    L = SQL.get(n, SQL[SQ_END - 1])
    if n == SQ_END:
        return [sess("A", [r5(0.4 * L)], memo="마지막 주 · 가볍게"),
                sess("B", [500], kind="측정", target=500, memo="최종 측정 · 6월 30일(수)")]
    if sq_ts(n):
        t = r5(SQL[n - 1] * 1.12)
        return [sess("A", [r5(0.5 * L)], memo="가벼운 주"),
                sess("B", [t], kind="측정", target=t, memo=f"연속 최대 · 목표 {t}")]
    if sq_dl(n): return [sess("A", [r5(0.6 * L)], memo="가벼운 주"), sess("B", [r5(0.3 * L)] * 2, rest=60)]
    return [sess("A", [L], memo="이번 주 연속 목표 · 한 번에"),
            sess("B", [r5(0.35 * L)] * 4, rest=60, memo="나눠서")]

# ── 2. 턱걸이 · 156주 (3년) · 주 2회 ─────────
PU_END = 156
pu_dl = lambda n: n % 4 == 0 and n < PU_END
pu_ts = lambda n: (n % 12 == 0 and n < PU_END) or n == PU_END
ASSIST_END = 30  # 1~30주 어시스트 40kg → 10kg (보조 0 이면 이미 맨몸 8회라 30주에 10kg 에서 멈춘다)
def assist(n): return r25(40 - 30 * (n - 1) / (ASSIST_END - 1))
def lin(first, last, a, b, dl, rnd):  # 턱걸이는 횟수를 고르게 늘린다 (비율로 늘리면 끝에 몰림)
    ws = [n for n in range(first, last + 1) if not dl(n)]
    out, cur = {}, None
    for i, n in enumerate(ws): out[n] = rnd(a + (b - a) * i / (len(ws) - 1))
    for n in range(first, last + 1):
        cur = out.get(n, cur); out[n] = cur
    return out
PUL = lin(31, PU_END - 1, 3, 45, pu_dl, ri)
def pu_week(n):
    if n <= ASSIST_END:
        a = assist(n)
        if pu_dl(n): return [sess("A", [8] * 3, a, 120, memo="가벼운 주 · 보조 kg"),
                             sess("B", [3] * 3, 0.0, 120, memo="내려오기만 5초씩 (네거티브)")]
        return [sess("A", [8] * 4, a, 150, memo="w = 보조 kg · 4×8 다 하면 다음 주로"),
                sess("B", [10] * 3, r25(a + 7.5), 120, memo="보조 조금 더 · 반복 많이")]
    L = PUL.get(n, PUL[PU_END - 1])
    if n == PU_END:
        return [sess("A", [ri(0.4 * L)] * 2, rest=180, memo="마지막 주 · 가볍게"),
                sess("B", [50], kind="측정", target=50, memo="최종 측정 · 체중 90kg 기준")]
    if pu_ts(n):
        t = min(50, ri(PUL[n - 1] * 1.15, 2))
        return [sess("A", [ri(0.5 * L)] * 2, rest=180, memo="가벼운 주"),
                sess("B", [t], kind="측정", target=t, memo=f"연속 최대 · 목표 {t}")]
    if pu_dl(n): return [sess("A", [ri(0.5 * L)] * 3, rest=180, memo="가벼운 주")]
    return [sess("A", [L, ri(0.7 * L), ri(0.6 * L), ri(0.5 * L)], rest=180, memo="첫 세트가 이번 주 연속 목표"),
            sess("B", [ri(0.4 * L, 2)] * 6, rest=90, memo="짧게 여러 번")]

# ── 3. 3대 운동 · 104주 (2년) · 4주 파도 ─────────
LIFTS = {"벤치프레스": (60, 120), "바벨 스쿼트": (100, 180), "데드리프트": (100, 220)}
LT_END = 104
def target(s, g, n):  # 처음엔 빨리, 뒤로 갈수록 천천히 오르는 목표 1RM
    t = (n - 1) / (LT_END - 1); return s + (g - s) * (1 - (1 - t) ** 1.6)
def lift_week(name, n):
    s, g = LIFTS[name]; T = target(s, g, n); p = (n - 1) % 4
    w = lambda f: r25(T * f)
    test = (n % 12 == 0) or n == LT_END
    one_day = name == "데드리프트"
    if test:
        tg = r25(T) if n < LT_END else g
        out = [sess("A", [5, 3, 1, 1], [w(.5), w(.7), w(.85), tg], 180, "측정", tg,
                    f"1RM 측정 · 목표 {tg:g}kg · 실패하면 그 전 무게가 기록")]
        return out
    if p == 3:
        A = sess("A", [5] * 3, w(.60), 120, memo="가벼운 주")
        return [A] if one_day else [A, sess("B", [5] * 3, w(.55), 120, memo="가벼운 주")]
    A = [sess("A", [5] * 5, w(.75), 180), sess("A", [4] * 4, w(.80), 180),
         sess("A", [3] * 5, w(.85), 210)][p]
    B = [sess("B", [8] * 3, w(.65), 120), sess("B", [8] * 3, w(.65), 120),
         sess("B", [6] * 3, w(.70), 120)][p]
    return [A] if one_day else [A, B]

def ex(name, part, eq, weeks, fn, goal, guide):
    return {"종목": name, "부위": part, "장비": eq, "목표": goal, "참고글": guide,
            "주": [{"주차": n, "시작": mon(n).isoformat(), "세션": fn(n)} for n in range(1, weeks + 1)]}

plan = {
    "형식": "hasenheide-plan", "형식버전": 2, "방식": "종목별",
    "설명": "루틴에 이 종목을 넣으면, 그날이 속한 주의 세션(A→B 순서)을 세트값으로 채운다. w=kg(어시스트 풀업은 보조 kg), r=횟수, 휴식값=초",
    "이름": "홍겸 · 턱걸이 50 / 맨몸 스쿼트 500 / 3대 운동", "시작": START.isoformat(),
    "기준": {"체중": 109, "목표체중": 90, "벤치": 60, "바벨스쿼트": 100, "데드리프트": 100,
            "어시스트풀업": "보조 40kg × 10회"},
    "진도규칙": ["처방을 다 채우면 다음 주로", "못 채우면 그 주를 한 번 더 (뒤 날짜가 밀림)",
                "측정 실패 → 직전 2주 반복 후 다시 측정", "통증이 다음 날까지 남으면 그 종목 한 주 쉬기"],
    "종목들": [
        ex("맨몸 스쿼트", "하체", "맨몸", SQ_END, sq_week, "연속 500 · 2027-06-30", "허벅지 수평 · 뒤꿈치 붙이고"),
        ex("턱걸이", "등", "맨몸", PU_END, pu_week, "연속 50 · 체중 90kg · 2029-09", "1~30주는 어시스트 풀업(w = 보조 kg, 40→10)"),
        *[ex(k, "가슴" if k == "벤치프레스" else ("하체" if "스쿼트" in k else "등"), "바벨", LT_END,
             (lambda n, k=k: lift_week(k, n)), f"1RM {LIFTS[k][1]}kg · 2028-09", "") for k in LIFTS],
    ],
}
if __name__ == "__main__":
    json.dump(plan, open("plan2.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    for e in plan["종목들"]:
        print(e["종목"], len(e["주"]), e["주"][-1]["시작"])
