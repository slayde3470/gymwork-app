"""3D 근육 모형(GLB) → 회색 클레이 그림 + 조각 번호 그림. 블렌더 4.5 안에서 돈다.

  blender --background --factory-startup --python 찍기.py -- 모형/full-body-male-mobile.glb 찍은것 [01 02 …]

그림마다 두 장을 찍는다.
  clay_NN.png  회색 클레이 그림 (Cycles · 배경 투명)
  id_NN.png    조각마다 다른 색을 준 그림 (Workbench · 테두리 섞임 없음) — 색 = 조각 번호
  idA_NN.png   힘줄 · 근막 덩어리를 숨기고 찍은 것 (막 바로 아래 근육을 알아내려고)
  idB_NN.png   배 옆 근육(외복사 · 내복사 · 복횡근)을 숨기고 찍은 것 (그 널힘줄에 덮인 복직근을 알아내려고)
  parts.json   조각 번호 → 모형의 근육 이름 · 쪽
조각 번호를 근육 나무 id · 색표 색으로 바꾸는 일은 지도만들기.py 가 한다.
좌표(블렌더): Z = 위, −Y = 몸 앞, +X = 사람의 왼쪽.
"""
import bpy, bmesh, sys, json, math, os
import numpy as np
from mathutils import Vector

args = sys.argv[sys.argv.index("--") + 1:]
GLB, OUT = os.path.abspath(args[0]), os.path.abspath(args[1])     # 블렌더는 상대 경로를 드라이브 맨 위로 본다 → 절대 경로로
ONLY = args[2:]

# ───────── 정한 값 (한곳에 모음) ─────────
배꼽높이 = 1.015        # 복근 위 · 아래를 가르는 높이(m). 짐작 — 키 1.708 의 약 0.59
엉덩관절 = (0.02, 0.865)   # 오른 다리 굽히기: 엉덩관절 (y, z)
무릎관절 = (0.03, 0.445)   # 무릎관절 (y, z)
엉덩굽힘, 무릎굽힘 = 62, 88   # 도
클레이색 = (0.50, 0.51, 0.49, 1)
다듬기단계 = 2          # 모형이 모바일용(삼각형 13만)이라 각져 보인다 → 면을 나눠 둥글게

# az: 0 = 앞, 180 = 뒤, + = 사람의 왼쪽으로 도는 쪽. span = 화면에 담기는 폭(m)
VIEWS = {
    "01": dict(kind="ortho", az=0, size=(768, 1376)),
    "02": dict(kind="ortho", az=180, size=(768, 1376)),
    "03": dict(kind="persp", az=45, el=4, target=(0.03, -0.03, 1.21), span=0.60, size=(1024, 1024)),
    "04": dict(kind="persp", az=55, el=4, target=(0.21, 0.00, 1.13), span=0.76, size=(1024, 1024)),
    "05": dict(kind="persp", az=-112, el=0, target=(-0.09, -0.15, 0.60), span=1.00, size=(1024, 1024), pose="leg"),
    "06": dict(kind="persp", az=180, el=4, target=(0.0, 0.05, 1.18), span=0.84, size=(1024, 1024)),
}
다리무리 = {"Glutes", "Hip flexors", "Hip rotators", "Quadriceps", "Hamstrings", "Adductors", "Sartorius", "Calves", "Lower legs"}
팔무리 = {"Biceps", "Triceps", "Upper arms", "Forearms"}
LEVELS = [0, 42, 85, 127, 170, 212, 255]   # 조각 번호를 색으로 적는 칸(7진법 세 자리)


def lin(c):
    c = c / 255
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4


# ───────── 불러오기 ─────────
bpy.ops.wm.read_factory_settings(use_empty=True)
bpy.ops.import_scene.gltf(filepath=GLB, merge_vertices=True)
scene = bpy.context.scene
objs = [o for o in bpy.data.objects if o.type == "MESH"]
for o in objs:
    me = o.data
    me.transform(o.matrix_world)
    o.matrix_world.identity()
    if "custom_normal" in me.attributes:          # 모양을 바꾸면 낡으므로 지운다
        me.attributes.remove(me.attributes["custom_normal"])
    me.polygons.foreach_set("use_smooth", [True] * len(me.polygons))
    me.update()

# ───────── 자세: 오른 다리 굽히기 (모형에 뼈대가 없어 꼭짓점을 직접 돌린다) ─────────
def islands(me):
    n = len(me.vertices)
    parent = list(range(n))

    def find(a):
        while parent[a] != a:
            parent[a] = parent[parent[a]]
            a = parent[a]
        return a
    for e in me.edges:
        a, b = find(e.vertices[0]), find(e.vertices[1])
        if a != b:
            parent[a] = b
    return np.array([find(i) for i in range(n)])


def smooth(t):
    t = np.clip(t, 0, 1)
    return t * t * (3 - 2 * t)


def rot_x(co, w, origin, deg):
    a = w * math.radians(deg)
    y, z = co[:, 1] - origin[0], co[:, 2] - origin[1]
    c, s = np.cos(a), np.sin(a)
    co[:, 1] = origin[0] + y * c - z * s
    co[:, 2] = origin[1] + y * s + z * c


def get_co(me):
    co = np.empty(len(me.vertices) * 3)
    me.vertices.foreach_get("co", co)
    return co.reshape(-1, 3)


def bend_leg():
    """엉덩관절 · 무릎을 굽힌다. 근육은 관절 둘레에서 부드럽게 휘고, 뼈는 덩어리째 돈다. 팔은 다리를 가려서 지운다."""
    for o in objs:
        me = o.data
        co = get_co(me)
        if o.get("key"):
            if o.get("group") in 팔무리:                 # 두 팔 다 지운다
                o.hide_render = True
                o.hide_viewport = True
                continue
            if o.get("side") != "right" or o.get("group") not in 다리무리:
                continue
            wk = smooth((무릎관절[1] + 0.07 - co[:, 2]) / 0.14)
            wh = smooth((엉덩관절[1] + 0.07 - co[:, 2]) / 0.17)
        else:
            isl = islands(me)
            wk = np.zeros(len(co))
            wh = np.zeros(len(co))
            kill = np.zeros(len(co), bool)
            bone = "bone" in o.name or "skeleton" in o.name
            for i in np.unique(isl):
                m = isl == i
                cx, cy, cz = co[m].mean(0)
                if abs(cx) > 0.14 and 0.6 < cz < 1.30:              # 두 팔 · 손
                    kill |= m
                elif -0.21 < cx < -0.02 and cz < 0.80:               # 오른 다리
                    if bone:
                        wh[m] = 1
                        wk[m] = 1 if cz < 0.50 else 0
                    else:
                        wk[m] = smooth((무릎관절[1] + 0.07 - co[m, 2]) / 0.14)
                        wh[m] = smooth((엉덩관절[1] + 0.07 - co[m, 2]) / 0.17)
            if kill.any():
                bm = bmesh.new()
                bm.from_mesh(me)
                bm.verts.ensure_lookup_table()
                bmesh.ops.delete(bm, geom=[bm.verts[j] for j in np.nonzero(kill)[0]], context="VERTS")
                keep = ~kill
                co, wk, wh = co[keep], wk[keep], wh[keep]
                bm.to_mesh(me)
                bm.free()
        rot_x(co, wk, 무릎관절, 무릎굽힘)          # 무릎: 정강이가 뒤로
        rot_x(co, wh, 엉덩관절, -엉덩굽힘)         # 엉덩관절: 허벅지가 앞으로
        me.vertices.foreach_set("co", co.ravel())
        me.update()



# 머리 · 손 · 발 덩어리에서 ① 넓은목근(목 앞을 덮는 얇은 막 근육)은 지운다 — 흉쇄유돌근 · 승모근을 가린다
# ② 목 앞 가운데 근육(목뿔 위 · 아래 근육 등)은 따로 떼어 '목 앞' 조각으로 만든다 — 지도에서 목을 칠할 수 있게
for o in list(objs):
    if "head_hands_feet" not in o.name:
        continue
    me = o.data
    co = get_co(me)
    isl = islands(me)
    kill = np.zeros(len(co), bool)
    neck = {"left": np.zeros(len(co), bool), "right": np.zeros(len(co), bool)}
    for i in np.unique(isl):
        m = isl == i
        p = co[m]
        c = p.mean(0)
        if m.sum() > 150 and 1.40 < c[2] < 1.50 and np.ptp(p[:, 0]) > 0.10 and p[:, 2].min() > 1.36:
            kill |= m
        elif 1.40 < c[2] < 1.515 and abs(c[0]) < 0.075 and c[1] < 0.035 and p[:, 2].max() < 1.56:
            neck["left" if c[0] >= 0 else "right"] |= m
    print("넓은목근 꼭짓점", int(kill.sum()), "목 앞 꼭짓점", int(neck["left"].sum()), int(neck["right"].sum()))
    for side, sel in neck.items():
        bm = bmesh.new()
        bm.from_mesh(me)
        bm.verts.ensure_lookup_table()
        bmesh.ops.delete(bm, geom=[bm.verts[j] for j in np.nonzero(~sel)[0]], context="VERTS")
        me2 = bpy.data.meshes.new("neck_front_" + side)
        bm.to_mesh(me2)
        bm.free()
        me2.polygons.foreach_set("use_smooth", [True] * len(me2.polygons))
        o2 = bpy.data.objects.new("neck_front_" + side, me2)
        scene.collection.objects.link(o2)
        o2["key"], o2["side"], o2["group"] = "neck_front", side, "Neck"
        objs.append(o2)
    bm = bmesh.new()
    bm.from_mesh(me)
    bm.verts.ensure_lookup_table()
    bmesh.ops.delete(bm, geom=[bm.verts[j] for j in np.nonzero(kill | neck["left"] | neck["right"])[0]], context="VERTS")
    bm.to_mesh(me)
    bm.free()

# 복직근은 모형에서 한 덩어리 → 배꼽 높이에서 위 · 아래 두 조각으로 자른다
parts = []      # (물체, 근육 이름, 쪽, 부분)
for o in list(objs):
    key = o.get("key")
    if key != "rectus_abdominis":
        parts.append((o, key, o.get("side", ""), ""))
        continue
    bm = bmesh.new()
    bm.from_mesh(o.data)
    bmesh.ops.bisect_plane(bm, geom=bm.verts[:] + bm.edges[:] + bm.faces[:], plane_co=(0, 0, 배꼽높이), plane_no=(0, 0, 1))
    low = bm.copy()
    bmesh.ops.delete(low, geom=[f for f in low.faces if f.calc_center_median().z >= 배꼽높이], context="FACES")
    bmesh.ops.delete(bm, geom=[f for f in bm.faces if f.calc_center_median().z < 배꼽높이], context="FACES")
    bm.to_mesh(o.data)
    me2 = bpy.data.meshes.new(o.name + "_low")
    low.to_mesh(me2)
    o2 = bpy.data.objects.new(o.name + "_low", me2)
    scene.collection.objects.link(o2)
    for k in ("key", "side", "group"):
        o2[k] = o[k]
    for m in (o.data, me2):
        m.polygons.foreach_set("use_smooth", [True] * len(m.polygons))
    objs.append(o2)
    parts.append((o, key, o["side"], "upper"))
    parts.append((o2, key, o["side"], "lower"))

# 조각 번호 = 색
table = []
for i, (o, key, side, part) in enumerate(parts, start=1):
    a, b, c = i // 49, (i // 7) % 7, i % 7
    o.color = (lin(LEVELS[a]), lin(LEVELS[b]), lin(LEVELS[c]), 1)
    table.append({"번호": i, "색": [LEVELS[a], LEVELS[b], LEVELS[c]], "이름": key or o.name.split("__")[0], "쪽": side, "부분": part,
                  "무리": o.get("group", ""), "근육": bool(key)})

clay = bpy.data.materials.new("clay")
clay.use_nodes = True
bsdf = clay.node_tree.nodes["Principled BSDF"]
bsdf.inputs["Base Color"].default_value = 클레이색
bsdf.inputs["Roughness"].default_value = 0.62
for o in objs:
    o.data.materials.clear()
    o.data.materials.append(clay)
    if 다듬기단계:
        m = o.modifiers.new("둥글게", "SUBSURF")
        m.levels = m.render_levels = 다듬기단계



# ───────── 카메라 · 빛 ─────────
allco = np.vstack([get_co(o.data) for o in objs])
ZTOP, ZBOT = allco[:, 2].max(), allco[:, 2].min()
cam = bpy.data.objects.new("cam", bpy.data.cameras.new("cam"))
scene.collection.objects.link(cam)
scene.camera = cam
rig = bpy.data.objects.new("rig", None)
scene.collection.objects.link(rig)
빛 = [("key", (-1.5, 1.5, 2.4), 125, 2.2), ("fill", (2.2, 0.2, 1.6), 22, 3.0), ("rim", (0.8, 1.8, -2.4), 90, 1.5)]   # 이름 · 자리 · 세기(W, 폭 1m 기준) · 크기
lights = []
for name, pos, watt, size in 빛:
    L = bpy.data.objects.new(name, bpy.data.lights.new(name, "AREA"))
    L.data.energy, L.data.size = watt, size
    L.parent = rig
    L.location = pos
    L.rotation_euler = Vector(pos).to_track_quat("Z", "Y").to_euler()     # 빛은 가운데(rig)를 본다
    scene.collection.objects.link(L)
    lights.append((L, watt))
world = bpy.data.worlds.new("w")
world.use_nodes = True
world.node_tree.nodes["Background"].inputs[0].default_value = (0.55, 0.56, 0.58, 1)
world.node_tree.nodes["Background"].inputs[1].default_value = 0.10
scene.world = world


def set_view(v):
    W, H = v["size"]
    scene.render.resolution_x, scene.render.resolution_y, scene.render.resolution_percentage = W, H, 100
    az = math.radians(v["az"])
    el = math.radians(v.get("el", 0))
    d = Vector((math.sin(az) * math.cos(el), -math.cos(az) * math.cos(el), math.sin(el)))
    right = Vector((math.cos(az), math.sin(az), 0))
    if v["kind"] == "ortho":
        # 전신: 머리 끝 y20 · 발끝 y1356 · 몸 가운데 x383 (1차 그림과 같은 자리)
        px = (ZTOP - ZBOT) / 1336
        cam.data.type = "ORTHO"
        cam.data.ortho_scale = max(W, H) * px
        target = Vector((0, 0, ZTOP - 668 * px)) + right * (0.5 * px)
        dist, span = 4.0, 1.8
    else:
        cam.data.type = "PERSP"
        cam.data.lens, cam.data.sensor_width = 85, 36
        target = Vector(v["target"])
        span = v["span"]
        dist = span * 85 / 36
    cam.location = target + d * dist
    cam.rotation_euler = (-d).to_track_quat("-Z", "Y").to_euler()
    cam.data.clip_start, cam.data.clip_end = 0.05, 50
    rig.location = target
    rig.rotation_euler = cam.rotation_euler
    s = max(span, 0.9)
    rig.scale = (s, s, s)
    for L, watt in lights:          # 가까이 당겨도 밝기가 같게
        L.data.energy = watt * s * s


def render(path, engine):
    r = scene.render
    r.engine = engine
    r.film_transparent = True
    r.image_settings.file_format, r.image_settings.color_mode, r.image_settings.color_depth = "PNG", "RGBA", "8"
    scene.view_settings.view_transform, scene.view_settings.look = "Standard", "None"
    scene.display_settings.display_device = "sRGB"
    if engine == "CYCLES":
        c = scene.cycles
        c.samples, c.use_denoising = 160, True
        c.device = "CPU"
        prefs = bpy.context.preferences.addons["cycles"].preferences
        for t in ("OPTIX", "CUDA"):
            try:
                prefs.compute_device_type = t
                prefs.get_devices()
                devs = [dv for dv in prefs.devices if dv.type == t]
                if devs:
                    for dv in prefs.devices:
                        dv.use = dv.type == t
                    c.device = "GPU"
                    break
            except Exception:
                pass
        print("CYCLES device", c.device, prefs.compute_device_type)
    else:
        sh = scene.display.shading
        sh.light, sh.color_type = "FLAT", "OBJECT"
        sh.show_shadows = sh.show_cavity = sh.show_object_outline = sh.show_specular_highlight = False
        scene.display.render_aa = "OFF"
    r.dither_intensity = 0
    r.filepath = path
    bpy.ops.render.render(write_still=True)


os.makedirs(OUT, exist_ok=True)
json.dump(table, open(os.path.join(OUT, "parts.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=0)
for name, v in sorted(VIEWS.items(), key=lambda t: bool(t[1].get("pose"))):       # 자세를 바꾸는 05 는 맨 뒤(되돌리지 않는다)
    if ONLY and name not in ONLY:
        continue
    if v.get("pose") == "leg":
        bend_leg()
    set_view(v)
    render(os.path.join(OUT, f"clay_{name}.png"), "CYCLES")
    render(os.path.join(OUT, f"id_{name}.png"), "BLENDER_WORKBENCH")
    for tag, hide in (("A", lambda o: "connective" in o.name), ("B", lambda o: o.get("key") in ("external_oblique", "internal_oblique", "transversus_abdominis"))):
        hidden = [o for o in objs if hide(o) and not o.hide_render]
        for o in hidden:
            o.hide_render = True
        render(os.path.join(OUT, f"id{tag}_{name}.png"), "BLENDER_WORKBENCH")
        for o in hidden:
            o.hide_render = False
    print("DONE", name)
