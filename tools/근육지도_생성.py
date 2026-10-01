#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
근육 그림 · 근육 나무 · 종목 규칙 → 앱 코드(data/MuscleData.kt) 만들기 (10-02)

언제 돌리나
  · 그림(SVG)을 바꿨을 때 · 근육을 더했을 때 · 종목 규칙이나 색표를 바꿨을 때
  · 돌린 뒤에는 시험(PlanTest)을 다시 돌리고 커밋한다

돌리는 법 (저장소 맨 위에서)
  python3 tools/근육지도_생성.py

읽는 것 — tools/근육지도/ (규칙: claude.ai 프로젝트 문서 claude/07_근육지도규칙.md)
  · hasenheide-body.svg  그림. <g id="view-front|view-back"> 안의
                         <path class="muscle" data-muscle=… data-covers=… data-side=…> · <path class="base"> ·
                         <g class="detail"> 안의 결 선
  · muscle-catalog.json  근육 나무 (regions · muscles[id, parent, name])
  · rules.json           역할 비중 · 종목 낱말 규칙 · 부위 기본값 · 색표 (7일 체험 아티팩트와 같은 표)

쓰는 것
  · app/src/main/java/com/slayde/hasenheide/data/MuscleData.kt  — 손으로 고치지 않는다
"""
import json
import os
import xml.etree.ElementTree as ET

뿌리 = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
원본 = os.path.join(뿌리, "tools", "근육지도")
결과 = os.path.join(뿌리, "app", "src", "main", "java", "com", "slayde", "hasenheide", "data", "MuscleData.kt")
SVG = "{http://www.w3.org/2000/svg}"


def 글(s):
    """코틀린 문자열 — 따옴표 · 역슬래시 · $ 를 막는다"""
    return '"' + str(s).replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'


def 조각읽기(경로):
    """SVG 를 문서 순서대로 훑어 조각 목록을 만든다 (그리는 순서 = 겹치는 순서)"""
    나무 = ET.parse(경로).getroot()
    out = []

    def 걷기(el, 보기, 결):
        tag = el.tag.replace(SVG, "")
        if tag == "g":
            i = el.get("id") or ""
            if i == "view-front":
                보기 = "f"
            elif i == "view-back":
                보기 = "b"
            if "detail" in (el.get("class") or "").split():
                결 = True
        if tag == "path":
            if 보기 is None:
                raise SystemExit("보기(view-front · view-back) 밖에 있는 path 가 있다: " + (el.get("d") or "")[:40])
            cls = (el.get("class") or "").split()
            d = " ".join((el.get("d") or "").split())
            if 결:
                out.append((보기, "x", "", [], "", d))
            elif "muscle" in cls:
                m = el.get("data-muscle")
                if not m:
                    raise SystemExit("data-muscle 이 없는 근육 조각: " + d[:40])
                대신 = (el.get("data-covers") or "").split()
                out.append((보기, "m", m, 대신, el.get("data-side") or "C", d))
            else:
                out.append((보기, "b", "", [], el.get("data-side") or "C", d))
        for ch in el:
            걷기(ch, 보기, 결)

    걷기(나무, None, False)
    return out


def main():
    목록 = json.load(open(os.path.join(원본, "muscle-catalog.json"), encoding="utf-8"))
    규칙 = json.load(open(os.path.join(원본, "rules.json"), encoding="utf-8"))
    조각 = 조각읽기(os.path.join(원본, "hasenheide-body.svg"))

    부위id = [r["id"] for r in 목록["regions"]]
    근육 = [m for m in 목록["muscles"] if not m.get("hidden")]
    아는id = set(부위id) | {m["id"] for m in 목록["muscles"]}

    # ── 검사 — 모르는 id 가 있으면 멈춘다 (07 3절) ──
    틀림 = []
    for v, k, m, c, s, d in 조각:
        for x in ([m] if m else []) + c:
            if x not in 아는id:
                틀림.append("그림: " + x)
    for 낱말, 표 in 규칙["규칙"]:
        틀림 += ["규칙 " + "/".join(낱말) + ": " + x for x in 표 if x not in 아는id]
    for p, 표 in 규칙["부위"].items():
        틀림 += ["부위 " + p + ": " + x for x in 표 if x not in 아는id]
    for m in 근육:
        if m["parent"] not in 아는id:
            틀림.append("부모: " + m["id"] + " → " + m["parent"])
    if 틀림:
        raise SystemExit("모르는 근육 id:\n  " + "\n  ".join(틀림))

    L = []
    w = L.append
    w("// ⚠ 자동으로 만든 파일 — 손으로 고치지 않는다.")
    w("// 만든 것: tools/근육지도_생성.py  ·  원본: tools/근육지도/ (hasenheide-body.svg · muscle-catalog.json · rules.json)")
    w("// 그림 · 근육 · 규칙을 바꾸면: python3 tools/근육지도_생성.py → 시험 → 커밋")
    w("// 규칙: claude.ai 프로젝트 문서 claude/07_근육지도규칙.md")
    w("package com.slayde.hasenheide.data")
    w("")
    w("internal object 근육자료 {")
    w("    /** 근육 → 부모 (부위 > 무리 > 근육 > 갈래). 부위(chest · back …)는 부모가 없다 */")
    w("    val 부모: Map<String, String> = mapOf(")
    for m in 근육:
        w("        %s to %s," % (글(m["id"]), 글(m["parent"])))
    w("    )")
    w("")
    w("    /** 근육 · 부위 id → 한글 이름 */")
    w("    val 이름: Map<String, String> = mapOf(")
    for r in 목록["regions"]:
        w("        %s to %s," % (글(r["id"]), 글(r["name"])))
    for m in 근육:
        w("        %s to %s," % (글(m["id"]), 글(m["name"])))
    w("    )")
    w("")
    w("    /** 그림 조각 — 그리는 순서대로. 좌표계 440 × 460, 뒷모습은 x + 240 (뒤 = true) */")
    w("    val 조각: List<몸조각> = listOf(")
    for v, k, m, c, s, d in 조각:
        대신 = "emptyList()" if not c else "listOf(" + ", ".join(글(x) for x in c) + ")"
        w("        몸조각(%s, '%s', %s, %s, %s, %s)," % ("true" if v == "b" else "false", k, 글(m), 대신, 글(s), 글(d)))
    w("    )")
    w("")
    w("    /** 역할 비중 — P 주동근 · S 보조근 · Y 협응근 (07 4절) */")
    w("    val 역할: Map<String, Double> = mapOf(" + ", ".join("%s to %s" % (글(k), float(v)) for k, v in 규칙["역할"].items()) + ")")
    w("")
    w("    /** 종목 이름 속 낱말 → 쓰는 근육과 역할. 위에서부터 먼저 맞는 것 하나 */")
    w("    val 규칙: List<Pair<List<String>, Map<String, String>>> = listOf(")
    for 낱말, 표 in 규칙["규칙"]:
        w("        listOf(%s) to mapOf(%s)," % (", ".join(글(x) for x in 낱말), ", ".join("%s to %s" % (글(a), 글(b)) for a, b in 표.items())))
    w("    )")
    w("")
    w("    /** 낱말 규칙에 없는 종목 — 종목의 부위로 */")
    w("    val 부위기본: Map<String, Map<String, String>> = mapOf(")
    for p, 표 in 규칙["부위"].items():
        w("        %s to mapOf(%s)," % (글(p), ", ".join("%s to %s" % (글(a), 글(b)) for a, b in 표.items())))
    w("    )")
    w("")
    w("    /** 색표 — (0~1 위치, 0xRRGGBB). 20단계는 이 사이를 이어 만든다 (07 4절) */")
    w("    val 색표: Map<String, List<Pair<Double, Int>>> = mapOf(")
    for n, st in 규칙["색표"].items():
        w("        %s to listOf(%s)," % (글(n), ", ".join("%s to 0x%s" % (float(a), c.lstrip("#").upper()) for a, c in st)))
    w("    )")
    w("}")
    w("")

    with open(결과, "w", encoding="utf-8") as f:
        f.write("\n".join(L))
    print("만들었다: %s — 근육 %d · 조각 %d (근육 %d) · 규칙 %d" % (
        os.path.relpath(결과, 뿌리), len(근육), len(조각), sum(1 for x in 조각 if x[1] == "m"), len(규칙["규칙"])))


if __name__ == "__main__":
    main()
