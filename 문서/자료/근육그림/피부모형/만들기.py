# 피부 모형 + 15칸: 해부 모형의 근육 자리를 피부 모형 겉면에 옮겨 칠하고, 칸 경계에 골을 파고 칸 안은 볼록하게 올린다
import sys, os, time
i = sys.argv.index("--")
src_path = sys.argv.pop(i + 1)
K = float(sys.argv.pop(i + 1))          # 키우는 세기: 0 = 도구 그대로, 1 = 표 그대로
ROOT = sys.argv.pop(i + 1)              # 근육그림 폴더 (칸표 · 색표)
pre, post = open(src_path, encoding="utf-8").read().split("os.makedirs(OUT, exist_ok=True)")
exec(pre)
from mathutils import Matrix
from mathutils.bvhtree import BVHTree
import addon_utils
addon_utils.enable("bl_ext.user_default.mpfb", default_set=True)
from bl_ext.user_default.mpfb.services.humanservice import HumanService
from bl_ext.user_default.mpfb.services.targetservice import TargetService
T0 = time.time()

# ───────── 체형 값 (한곳에) ─────────
MACRO = dict(gender=1.0, age=0.5, muscle=1.0, weight=0.9, proportions=0.8, height=0.5)
YC = 0.02
TZ   = [0.78, 0.86, 0.94, 1.03, 1.15, 1.27, 1.38, 1.45, 1.52]
T_SX = [1.00, 1.16, 1.20, 1.00, 1.06, 1.16, 1.14, 1.08, 1.00]      # 몸통 좌우
T_SF = [1.00, 1.00, 1.00, 1.00, 1.08, 1.28, 1.22, 1.08, 1.00]      # 몸통 앞쪽(가슴)
T_SB = [1.15, 1.85, 1.90, 1.10, 1.06, 1.14, 1.14, 1.06, 1.00]      # 몸통 뒤쪽(등 · 엉덩이)
ARM_U = [0.0, 0.2, 0.55, 0.95, 1.0, 1.25, 1.6, 1.9, 2.0]
ARM_F = [1.32, 1.42, 1.50, 1.35, 1.32, 1.62, 1.48, 1.18, 1.08]
CALF_U = [0.0, 0.12, 0.35, 0.7, 1.0]
CALF_F = [1.06, 1.18, 1.22, 1.12, 1.02]
ARM_DOWN, ELBOW_STRAIGHT = 28, 26
# 자세: 어깨를 뒤로 · 가슴을 펴고 · 목을 세운다 (도)
SHOULDER_BACK, CHEST_UP, NECK_UP = 9, 4, 5
# 칸마다 볼록하게 올리는 높이(m) · 골 깊이
DOME = {"가슴": 0.018, "엉덩이": 0.022, "종아리": 0.003, "어깨": 0.005, "이두": 0.009, "삼두": 0.006, "전완": 0.005, "복부": 0.004,
        "승모": 0.006, "광배": 0.010, "허리": 0.004, "대퇴사두": 0.006, "햄스트링": 0.006, "내전근": 0.004, "목": 0.002}
DOME_WIDE = {"엉덩이": 0.055, "가슴": 0.040}     # 넓게 둥근 칸
GROOVE, GROOVE_W, DOME_W = 0.005, 0.009, 0.030
CELLS = {
    "목": ["sternocleidomastoid", "scalenus_medius", "scalenus_anterior", "scalenus_posterior", "splenius_capitis", "splenius_colli", "levator_scapulae", "neck_front"],
    "어깨": ["deltoid_anterior", "deltoid_lateral", "deltoid_posterior"],
    "가슴": ["pectoralis_major_clavicular", "pectoralis_major_sternocostal", "pectoralis_major_abdominal"],
    "이두": ["biceps_brachii_long", "biceps_brachii_short", "brachialis", "coracobrachialis"],
    "삼두": ["triceps_long", "triceps_lateral", "triceps_medial"],
    "전완": ["@Forearms"],
    "복부": ["rectus_abdominis", "external_oblique", "internal_oblique", "transversus_abdominis"],
    "승모": ["trapezius_upper", "trapezius_middle", "trapezius_lower"],
    "광배": ["latissimus_dorsi", "teres_major"],
    "허리": [],                      # 척추기립근 — 광배 널힘줄 밑이라 따로 규칙으로 찾는다
    "엉덩이": ["gluteus_maximus"],   # 옆으로 삐져나온 살(중둔근 · 대퇴근막장근 자리)과 엉덩이골 위는 칠하지 않는다
    "대퇴사두": ["rectus_femoris", "vastus_lateralis", "vastus_medialis", "vastus_intermedius", "sartorius"],
    "햄스트링": ["biceps_femoris_long", "biceps_femoris_short", "semitendinosus", "semimembranosus"],
    "내전근": ["adductor_magnus", "adductor_longus", "adductor_brevis", "gracilis", "pectineus"],
    "종아리": ["@Calves", "@Lower legs"],
}
ERECT = {"iliocostalis_lumborum", "iliocostalis_thoracis", "longissimus_thoracis", "spinalis_thoracis", "multifidus_lumborum"}
NAMES = list(CELLS)
# 해부 모형의 관절 자리(왼쪽, m) — 뼈 덩어리에서 읽은 값
AJ = {"S": np.array([0.160, 0.030, 1.390]), "E": np.array([0.235, 0.035, 1.095]), "W": np.array([0.275, 0.0, 0.845]),
      "H": np.array([0.082, 0.020, 0.865]), "K": np.array([0.078, 0.030, 0.445]), "A": np.array([0.085, 0.035, 0.075])}
FRONT = np.array([0.0, -1.0, 0.0])


def mix(f):
    return 1 + (np.asarray(f, float) - 1) * K


T_SX, T_SF, T_SB, ARM_F, CALF_F = mix(T_SX), mix(T_SF), mix(T_SB), mix(ARM_F), mix(CALF_F)


def seg(p, A, B):
    d = B - A
    t = np.clip(((p - A) @ d) / (d @ d), 0, 1)
    return A + t[:, None] * d, t


def radial2(p, P0, P1, P2, us, fs):
    d1, d2 = P1 - P0, P2 - P1
    t1 = np.clip(((p - P0) @ d1) / (d1 @ d1), 0, 1.35)
    t2 = np.clip(((p - P1) @ d2) / (d2 @ d2), -0.35, 1)
    c1, c2 = P0 + t1[:, None] * d1, P1 + t2[:, None] * d2
    u1, u2 = np.minimum(t1, 1), 1 + np.maximum(t2, 0)
    q1 = c1 + (p - c1) * np.interp(u1, us, fs)[:, None]
    q2 = c2 + (p - c2) * np.interp(u2, us, fs)[:, None]
    b = d1 / np.linalg.norm(d1) + d2 / np.linalg.norm(d2)
    w = smooth(((p - P1) @ (b / np.linalg.norm(b)) + 0.06) / 0.12)
    dist = (1 - w) * np.sqrt(((p - c1) ** 2).sum(1)) + w * np.sqrt(((p - c2) ** 2).sum(1))
    return q1 + w[:, None] * (q2 - q1), dist, (1 - w) * u1 + w * u2


def t_torso(p):
    q = p.copy()
    q[:, 0] *= np.interp(p[:, 2], TZ, T_SX)
    f = np.where(p[:, 1] < YC, np.interp(p[:, 2], TZ, T_SF), np.interp(p[:, 2], TZ, T_SB))
    q[:, 1] = YC + (p[:, 1] - YC) * f
    return q


def bulk(co, J):
    sgn = np.where(co[:, 0] < 0, -1.0, 1.0)
    p = co.copy()
    p[:, 0] = np.abs(p[:, 0])
    q = t_torso(p)
    qa, dist, u = radial2(p, J["S"], J["E"], J["W"], ARM_U, ARM_F)
    qa += t_torso(J["S"][None, :])[0] - J["S"]
    along = ((p - J["S"]) @ (J["E"] - J["S"])) / np.linalg.norm(J["E"] - J["S"]) ** 2
    wa = smooth((along + 0.30) / 0.40) * smooth((0.135 - dist) / 0.05)
    wa = np.where(u > 0.6, np.maximum(wa, smooth((0.16 - dist) / 0.05)), wa)
    q = q + wa[:, None] * (qa - q)
    c, t = seg(p, J["K"], J["A"])
    qc = c + (p - c) * np.interp(t, CALF_U, CALF_F)[:, None]
    wc = smooth((J["K"][2] + 0.02 - p[:, 2]) / 0.08) * (p[:, 2] > J["A"][2] - 0.02)
    q = q + wc[:, None] * (qc - q)
    q[:, 0] *= sgn
    return q


# ═════════ 1. 피부 모형 ═════════
macro = TargetService.get_default_macro_info_dict()
macro.update(MACRO)
body = HumanService.create_human(mask_helpers=False, detailed_helpers=True, extra_vertex_groups=True, feet_on_ground=True, scale=0.1, macro_detail_dict=macro)
TargetService.bake_targets(body)


def joint(name):
    gi = body.vertex_groups[name].index
    idx = [v.index for v in body.data.vertices if any(g.group == gi and g.weight > 0.5 for g in v.groups)]
    return get_co(body.data)[idx].mean(0)


JN = {"S": "joint-l-shoulder", "E": "joint-l-elbow", "W": "joint-l-hand", "H": "joint-l-upper-leg", "K": "joint-l-knee", "A": "joint-l-ankle",
      "neck": "joint-neck", "head": "joint-head"}
J = {k: joint(v) for k, v in JN.items()}
body.data.vertices.foreach_set("co", bulk(get_co(body.data), J).ravel())
body.data.update()
SJ = {k: joint(v) for k, v in JN.items()}          # 키운 뒤의 관절 자리
print("SKIN JOINTS", {k: v.round(3).tolist() for k, v in SJ.items()})

arm = HumanService.add_builtin_rig(body, "default")
gi = body.vertex_groups["body"].index
inbody = np.array([any(g.group == gi and g.weight > 0.5 for g in v.groups) for v in body.data.vertices])
bm = bmesh.new(); bm.from_mesh(body.data); bm.verts.ensure_lookup_table()
bmesh.ops.delete(bm, geom=[bm.verts[j] for j in np.nonzero(~inbody)[0]], context="VERTS")
bm.to_mesh(body.data); bm.free()
# 잘게 나눈다(약 21만 꼭짓점) — 칸 경계 · 골을 매끈하게 그리려고
bpy.context.view_layer.objects.active = body
body.select_set(True)
sub = body.modifiers.new("나누기", "SUBSURF"); sub.levels = 2
with bpy.context.temp_override(object=body, active_object=body, selected_objects=[body], selected_editable_objects=[body]):
    bpy.ops.object.modifier_apply(modifier=sub.name)
me = body.data
me.polygons.foreach_set("use_smooth", [True] * len(me.polygons))
V = get_co(me)
NV = len(V)
nrm = np.empty(NV * 3); me.vertex_normals.foreach_get("vector", nrm); nrm = nrm.reshape(-1, 3)
print("SKIN verts", NV, "polys", len(me.polygons), "t=%.0f" % (time.time() - T0))

# 뼈 무게로 몸 부분을 가른다 (피부 쪽 굵기 · 폭을 재려고)
def cat_of(name):
    n = name.lower()
    if n.startswith("upperarm"): return 1
    if n.startswith("lowerarm"): return 2
    if n.startswith(("wrist", "finger", "metacarpal", "palm")): return 3
    if n.startswith("upperleg"): return 4
    if n.startswith("lowerleg"): return 5
    if n.startswith(("foot", "toe")): return 6
    return 0
bones = {b.name for b in arm.data.bones}
gcat = np.array([cat_of(g.name) if g.name in bones else -1 for g in body.vertex_groups])
wcat = np.zeros((NV, 7))
for v in me.vertices:
    for g in v.groups:
        c = gcat[g.group]
        if c >= 0:
            wcat[v.index, c] += g.weight
wcat /= np.maximum(wcat.sum(1, keepdims=True), 1e-9)
Vm = V.copy(); Vm[:, 0] = np.abs(Vm[:, 0])

# ═════════ 2. 해부 모형을 피부 모형에 맞춘다 ═════════
ZA = [0.007, AJ["A"][2], AJ["K"][2], 0.80, AJ["H"][2], 1.015, 1.22, AJ["S"][2], 1.47, 1.56, 1.708]
ZS = [0.0, SJ["A"][2], SJ["K"][2], 0.85, SJ["H"][2], 1.05, 1.244, SJ["S"][2], SJ["neck"][2], SJ["head"][2], V[:, 2].max()]
print("Z knots", np.round(ZS, 3))
LEGS = {"Quadriceps", "Hamstrings", "Adductors", "Sartorius", "Calves", "Lower legs"}
A_co, A_cls, A_part, A_polys, A_poly_part = [], [], [], [], []
part_names = []
off = 0
for o in objs:
    m_ = o.data
    co = get_co(m_)
    key = o.get("key")
    if key:
        g = o.get("group")
        cls = np.full(len(co), 1 if g in 팔무리 else 2 if g in LEGS else 0)
        nodelt = key.startswith("deltoid")
    else:
        isl = islands(m_)
        cls = np.zeros(len(co), int)
        for i_ in np.unique(isl):
            mm = isl == i_
            cx, cy, cz = co[mm].mean(0)
            cls[mm] = 1 if (abs(cx) > 0.14 and 0.6 < cz < 1.30) else 2 if (abs(cx) < 0.21 and cz < 0.80) else 0
        nodelt = False
    pid = len(part_names)
    part_names.append((key or o.name.split("__")[0], o.get("group", "")))
    A_co.append(co); A_cls.append(cls); A_part.append(np.full(len(co), pid))
    for pl in m_.polygons:
        A_polys.append([off + vi for vi in pl.vertices]); A_poly_part.append(pid)
    off += len(co)
A_co, A_cls, A_part = np.vstack(A_co), np.concatenate(A_cls), np.concatenate(A_part)
A_delt = np.array([part_names[p][0].startswith("deltoid") for p in A_part])
Am = A_co.copy(); Am[:, 0] = np.abs(Am[:, 0])


def frame(u):
    f = FRONT - (FRONT @ u) * u
    f /= np.linalg.norm(f)
    return f, np.cross(u, f)


def env(P, P0, P1, sel_extra=None, nb=10, pct=95):
    """마디 둘레의 바깥 반지름(구간별)."""
    d = P1 - P0
    t = ((P - P0) @ d) / (d @ d)
    r = np.linalg.norm(P - P0 - t[:, None] * d, axis=1)
    out = np.full(nb, np.nan)
    for b in range(nb):
        s = (t >= b / nb) & (t < (b + 1) / nb) & (r < 0.22)
        if s.sum() > 20:
            out[b] = np.percentile(r[s], pct)
    x = (np.arange(nb) + 0.5) / nb
    ok = ~np.isnan(out)
    return x, np.interp(x, x[ok], out[ok])


def kfun(skin_sel, atlas_sel, a, b):
    xs, rs = env(Vm[skin_sel], SJ[a], SJ[b])
    xa, ra = env(Am[atlas_sel], AJ[a], AJ[b])
    k = np.clip(rs / ra, 0.8, 2.2) * 0.95
    k[:4] = np.minimum(k[:4], k[4])
    k = np.convolve(np.pad(k, 1, mode="edge"), np.ones(3) / 3, mode="valid")
    print("  굵기 비 %s-%s" % (a, b), np.round(k, 2))
    return xs, k


KU = kfun(wcat[:, 1] > 0.6, A_cls == 1, "S", "E")
KF = kfun(wcat[:, 2] > 0.6, A_cls == 1, "E", "W")
KT = kfun(wcat[:, 4] > 0.6, A_cls == 2, "H", "K")
KC = kfun(wcat[:, 5] > 0.6, A_cls == 2, "K", "A")


def limb_map(p, a0, a1, a2, k1, k2):
    A0, A1, A2, B0, B1, B2 = AJ[a0], AJ[a1], AJ[a2], SJ[a0], SJ[a1], SJ[a2]
    out = []
    for (P0, P1, Q0, Q1, kk, lo, hi) in ((A0, A1, B0, B1, k1, -0.35, 1.35), (A1, A2, B1, B2, k2, -0.35, 1.6)):
        da, db = P1 - P0, Q1 - Q0
        ua, ub = da / np.linalg.norm(da), db / np.linalg.norm(db)
        t = np.clip(((p - P0) @ da) / (da @ da), lo, hi)
        r = p - (P0 + t[:, None] * da)
        fa, ga = frame(ua); fb, gb = frame(ub)
        k = np.interp(np.clip(t, 0, 1), kk[0], kk[1])[:, None]
        out.append(Q0 + t[:, None] * db + k * ((r @ fa)[:, None] * fb + (r @ ga)[:, None] * gb) + (r @ ua)[:, None] * ub)
    d1, d2 = A1 - A0, A2 - A1
    b = d1 / np.linalg.norm(d1) + d2 / np.linalg.norm(d2)
    w = smooth(((p - A1) @ (b / np.linalg.norm(b)) + 0.06) / 0.12)
    return out[0] + w[:, None] * (out[1] - out[0])


# 몸통: 높이를 맞추고, 높이마다 좌우 폭 · 앞뒤 끝을 피부 모형에 맞춘다
zb = np.arange(0.78, V[:, 2].max() + 0.03, 0.03)
def prof(P, sel):
    xs, yf, yb = [], [], []
    for z in zb:
        s = sel & (np.abs(P[:, 2] - z) < 0.025)
        if s.sum() < 30:
            xs.append(np.nan); yf.append(np.nan); yb.append(np.nan); continue
        xs.append(np.percentile(P[s, 0], 97)); yf.append(np.percentile(P[s, 1], 3)); yb.append(np.percentile(P[s, 1], 97))
    out = []
    for a_ in (xs, yf, yb):
        a_ = np.array(a_); ok = ~np.isnan(a_)
        a_ = np.interp(zb, zb[ok], a_[ok])
        out.append(np.convolve(np.pad(a_, 1, mode="edge"), np.ones(3) / 3, mode="valid"))
    return out
Az = Am.copy(); Az[:, 2] = np.interp(Am[:, 2], ZA, ZS)
aX, aF, aB = prof(Az, (A_cls == 0) & ~A_delt)
sX, sF, sB = prof(Vm, wcat[:, 0] > 0.6)
rX = np.clip(sX / aX, 0.9, 1.7)
lo = zb < 0.88                                  # 샅 아래는 잴 것이 없다 → 바로 위 값을 쓴다
rX[lo] = rX[~lo][0]; sF[lo] = sF[~lo][0]; sB[lo] = sB[~lo][0]; aF[lo] = aF[~lo][0]; aB[lo] = aB[~lo][0]
print("  몸통 폭 비", np.round(rX, 2)[::3])


def torso_map(p):
    q = p.copy()
    q[:, 2] = np.interp(p[:, 2], ZA, ZS)
    z = q[:, 2]
    q[:, 0] = p[:, 0] * np.interp(z, zb, rX)
    fa, ba, fs, bs = np.interp(z, zb, aF), np.interp(z, zb, aB), np.interp(z, zb, sF), np.interp(z, zb, sB)
    q[:, 1] = bs + (p[:, 1] - ba) * (fs - bs) / (fa - ba)
    return q


qa = limb_map(Am, "S", "E", "W", KU, KF)
ql = limb_map(Am, "H", "K", "A", KT, KC)
qt = torso_map(Am)
x, z = Am[:, 0], Am[:, 2]
wa = smooth((x - 0.125) / 0.075) * smooth((z - 1.22) / 0.10) * smooth((1.52 - z) / 0.06)
wl = smooth((0.97 - z) / 0.14) * smooth(x / 0.05)
Q = qt + wa[:, None] * (qa - qt) + wl[:, None] * (ql - qt)
Q[A_cls == 1] = qa[A_cls == 1]
Q[A_cls == 2] = ql[A_cls == 2]
Q[:, 0] *= np.where(A_co[:, 0] < 0, -1.0, 1.0)
bvh = BVHTree.FromPolygons([tuple(v) for v in Q], A_polys, all_triangles=False, epsilon=0.0)
A_poly_part = np.array(A_poly_part)
print("ATLAS fitted, t=%.0f" % (time.time() - T0))
# 맞춘 해부 모형을 눈으로 볼 수 있게 물체에도 적어 둔다
off = 0
for o in objs:
    n_ = len(o.data.vertices)
    o.data.vertices.foreach_set("co", Q[off:off + n_].ravel()); o.data.update(); off += n_
    for md in list(o.modifiers):
        o.modifiers.remove(md)

# ═════════ 3. 피부 꼭짓점마다: 바로 밑에 어느 근육이 있나 ═════════
key2cell = {}
for ci, (cn, keys) in enumerate(CELLS.items(), start=1):
    for k_ in keys:
        key2cell[k_] = ci
part_cell = np.array([key2cell.get(k_, key2cell.get("@" + g_, 0)) for k_, g_ in part_names])
part_kind = np.array([2 if k_ == "connective_tissue" else 1 if k_ in ERECT else 3 if k_ == "latissimus_dorsi" else 0 for k_, g_ in part_names])
LUMBAR = (SJ["H"][2] + 0.03, 1.05 + 0.15, 0.085)          # 허리 칸: 높이 범위 · 가운데에서의 폭
label = np.zeros(NV, np.int16)
HURI = NAMES.index("허리") + 1
OFF, DEPTH = 0.04, 0.20
headz = SJ["head"][2] + 0.01
for vi in range(NV):
    if V[vi, 2] > headz or wcat[vi, 3] > 0.5 or wcat[vi, 6] > 0.5:      # 머리 · 손 · 발은 칠하지 않는다
        continue
    pos = V[vi].copy()
    if abs(pos[0]) < 0.014:
        pos[0] = 0.014 if pos[0] >= 0 else -0.014
    lumbar = LUMBAR[0] < pos[2] < LUMBAR[1] and nrm[vi, 1] > 0.3 and abs(pos[0]) < LUMBAR[2] * (1 - 0.45 * (pos[2] - LUMBAR[0]) / (LUMBAR[1] - LUMBAR[0]))
    o_ = Vector(pos + nrm[vi] * OFF)
    d_ = Vector(-nrm[vi])
    hits = []
    start, left = o_, DEPTH
    for _ in range(7):
        loc, nor, idx, dist = bvh.ray_cast(start, d_, left)
        if loc is None:
            break
        if nor.dot(d_) < 0:                                   # 겉에서 들어가는 면만
            hits.append((A_poly_part[idx], (loc - o_).length))
            if len(hits) >= 3:
                break
        start = loc + d_ * 0.0008
        left = DEPTH - (start - o_).length
        if left <= 0:
            break
    if not hits:
        continue
    p0, d0 = hits[0]
    if part_kind[p0] == 2:                                   # 힘줄 · 근막: 바로 밑이 근육이면 그 근육
        nxt = [(p, d) for p, d in hits[1:] if d - d0 < 0.025 and part_kind[p] != 2]
        if not nxt:
            if lumbar:
                label[vi] = HURI
            continue
        p0, d0 = nxt[0]
    if lumbar and part_kind[p0] in (1, 3):                    # 허리: 광배 널힘줄 · 기립근 자리
        label[vi] = HURI
        continue
    label[vi] = part_cell[p0]
JONG = NAMES.index("종아리") + 1
shin = (label == 0) & (wcat[:, 5] > 0.5) & (V[:, 2] > SJ["A"][2] + 0.07) & (V[:, 2] < SJ["K"][2] - 0.07)
label[shin] = JONG
print("LABELS", {NAMES[c - 1]: int((label == c).sum()) for c in range(1, len(NAMES) + 1)}, "none", int((label == 0).sum()), "t=%.0f" % (time.time() - T0))

# 좌우를 따로 (L = 사람의 왼쪽 = +X) → 칸 번호: 0 없음, 1..15 왼쪽, 16..30 오른쪽
NC = len(NAMES)
lab = np.where(label > 0, label + np.where(V[:, 0] < 0, NC, 0), 0).astype(np.int32)
ed = np.empty(len(me.edges) * 2, np.int32); me.edges.foreach_get("vertices", ed); ed = ed.reshape(-1, 2)
deg = np.zeros(NV); np.add.at(deg, ed[:, 0], 1); np.add.at(deg, ed[:, 1], 1)
P = np.zeros((NV, 2 * NC + 1), np.float32)
P[np.arange(NV), lab] = 1
for it in range(14):                                          # 칸마다 부드럽게 번지게(약 2cm) → 가장 센 칸
    S_ = np.zeros_like(P)
    np.add.at(S_, ed[:, 0], P[ed[:, 1]])
    np.add.at(S_, ed[:, 1], P[ed[:, 0]])
    P = 0.5 * P + 0.5 * S_ / deg[:, None]
lab = P.argmax(1).astype(np.int32)
del P, S_
# 작은 섬(외딴 조각)은 둘레에서 가장 많은 칸으로 바꾼다
parent = np.arange(NV)
def find(a):
    while parent[a] != a:
        parent[a] = parent[parent[a]]
        a = parent[a]
    return a
same = ed[lab[ed[:, 0]] == lab[ed[:, 1]]]
for a_, b_ in same:
    ra, rb = find(a_), find(b_)
    if ra != rb:
        parent[ra] = rb
root = np.array([find(i_) for i_ in range(NV)])
ids, inv, cnts = np.unique(root, return_inverse=True, return_counts=True)
small = cnts[inv] < 600
if small.any():
    for comp in np.unique(inv[small]):
        vs = np.nonzero(inv == comp)[0]
        vset = np.zeros(NV, bool); vset[vs] = True
        nb = np.concatenate([lab[ed[vset[ed[:, 0]] & ~vset[ed[:, 1]], 1]], lab[ed[vset[ed[:, 1]] & ~vset[ed[:, 0]], 0]]])
        if len(nb):
            lab[vs] = np.bincount(nb).argmax()
print("CELLS after cleanup", {NAMES[c - 1]: int(((lab == c) | (lab == c + NC)).sum()) for c in range(1, NC + 1)}, "t=%.0f" % (time.time() - T0))

# ═════════ 4. 칸 경계에 골 · 칸 안은 볼록 ═════════
from mathutils.kdtree import KDTree
bidx = np.unique(ed[lab[ed[:, 0]] != lab[ed[:, 1]]].ravel())
kd = KDTree(len(bidx))
for n_, vi in enumerate(bidx):
    kd.insert(Vector(V[vi]), n_)
kd.balance()
dist = np.array([kd.find(Vector(V[vi]))[2] for vi in range(NV)])
cell = np.where(lab > NC, lab - NC, lab)
dome = np.array([0.0] + [DOME[n] for n in NAMES])[cell]
domew = np.array([DOME_W] + [DOME_WIDE.get(n, DOME_W) for n in NAMES])[cell]
disp = -GROOVE * np.exp(-(dist / GROOVE_W) ** 2) + dome * (1 - np.exp(-(dist / domew) ** 2))
for it in range(6):                                          # 계단 진 경계를 따라 생기는 잔 굴곡을 편다
    S_ = np.zeros(NV); np.add.at(S_, ed[:, 0], disp[ed[:, 1]]); np.add.at(S_, ed[:, 1], disp[ed[:, 0]])
    disp = 0.5 * disp + 0.5 * S_ / deg
V2 = V + nrm * disp[:, None]
me.vertices.foreach_set("co", V2.ravel())
me.update()
print("SCULPT done t=%.0f" % (time.time() - T0))

# 면마다 칸 재질(조각 번호 그림용). 색 = 색표의 대표 색
칸표 = {k_["이름"]: k_["대표"] for k_ in json.load(open(os.path.join(ROOT, "대표칸", "칸표.json"), encoding="utf-8"))["칸"]}
색표 = {(v_["근육"], v_["쪽"]): k_ for k_, v_ in json.load(open(os.path.join(ROOT, "색표.json"), encoding="utf-8"))["색"].items()}
me.materials.clear()
m0 = bpy.data.materials.new("none"); m0.diffuse_color = (0, 0, 0, 1); me.materials.append(m0)
legend = {}
for side in ("L", "R"):
    for n_ in NAMES:
        hx = 색표[(칸표[n_], side)]
        rgb = [int(hx[j:j + 2], 16) for j in (1, 3, 5)]
        mt = bpy.data.materials.new(n_ + side); mt.diffuse_color = (lin(rgb[0]), lin(rgb[1]), lin(rgb[2]), 1)
        me.materials.append(mt); legend[hx] = [n_, side]
lv = np.empty(len(me.loops), np.int32); me.loops.foreach_get("vertex_index", lv)
ls = np.empty(len(me.polygons), np.int32); me.polygons.foreach_get("loop_start", ls)
me.polygons.foreach_set("material_index", lab[lv[ls]])
me.update()

# ═════════ 5. 자세 · 찍기 ═════════
def turn(name, axis, deg):
    pb = arm.pose.bones[name]
    M = arm.matrix_world @ pb.matrix
    head = M.translation.copy()
    pb.matrix = arm.matrix_world.inverted() @ (Matrix.Translation(head) @ Matrix.Rotation(math.radians(deg), 4, axis) @ Matrix.Translation(-head) @ M)
    bpy.context.view_layer.update()


def bone_dir(name):
    pb = arm.pose.bones[name]
    return ((arm.matrix_world @ pb.tail) - (arm.matrix_world @ pb.head)).normalized()


turn("spine02", Vector((1, 0, 0)), -CHEST_UP)
turn("neck01", Vector((1, 0, 0)), -NECK_UP)
for s, sg in (("L", 1), ("R", -1)):
    turn("clavicle." + s, Vector((0, 0, 1)), sg * SHOULDER_BACK)
    turn("upperarm01." + s, Vector((0, 1, 0)), sg * ARM_DOWN)
    up, fo = bone_dir("upperarm02." + s), bone_dir("lowerarm01." + s)
    ax = fo.cross(up)
    if ax.length > 1e-6:
        turn("lowerarm01." + s, ax.normalized(), ELBOW_STRAIGHT)
for o in objs:
    o.hide_render = True
# 자세를 바꾸면 잘게 나눈 면에 잔주름이 생긴다 → 원래 모양을 기준으로 펴 준다
cs = body.modifiers.new("주름펴기", "CORRECTIVE_SMOOTH")
cs.iterations, cs.factor, cs.rest_source, cs.smooth_type, cs.use_only_smooth = 12, 0.6, "ORCO", "SIMPLE", False
dg = bpy.context.evaluated_depsgraph_get()
ev = body.evaluated_get(dg)
wc = np.array([ev.matrix_world @ v.co for v in ev.data.vertices])
ZTOP, ZBOT = wc[:, 2].max(), wc[:, 2].min()
print("H", ZTOP, ZBOT, "W px", (wc[:, 0].max() - wc[:, 0].min()) / ((ZTOP - ZBOT) / 1336))
os.makedirs(OUT, exist_ok=True)
json.dump(legend, open(os.path.join(OUT, "legend.json"), "w", encoding="utf-8"), ensure_ascii=False)
vl = bpy.context.view_layer
for n, az in (("01", 0), ("02", 180), ("side", -90)):
    set_view(dict(kind="ortho", az=az, size=(1000, 1376)))
    vl.material_override = clay
    render(os.path.join(OUT, f"clay_{n}.png"), "CYCLES")
    vl.material_override = None
    r_ = scene.render
    r_.engine = "BLENDER_WORKBENCH"
    sh = scene.display.shading
    sh.light, sh.color_type = "FLAT", "MATERIAL"
    sh.show_shadows = sh.show_cavity = sh.show_object_outline = sh.show_specular_highlight = False
    scene.display.render_aa = "OFF"
    r_.dither_intensity = 0
    r_.filepath = os.path.join(OUT, f"id_{n}.png")
    bpy.ops.render.render(write_still=True)
bpy.ops.wm.save_as_mainfile(filepath=os.path.join(OUT, "skin3.blend"))
print("DONE t=%.0f" % (time.time() - T0))
