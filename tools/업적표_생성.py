#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
업적표(CSV) → 앱 코드(data/AchievementData.kt) 만들기 (10-02)

언제 돌리나
  · 업적 · 칭호 · 문구 · 플레이버를 고쳤을 때 (고칠 곳은 claude.ai 프로젝트 문서 12-10 번호판 → 그 다음 이 CSV)
  · 돌린 뒤에는 시험(PlanTest · StatsTest)을 다시 돌리고 커밋한다

돌리는 법 (저장소 맨 위에서)
  python3 tools/업적표_생성.py

읽는 것 — tools/업적표/업적표.csv (UTF-8, 107줄)
  열: 번호 · 칭호번호 · 이름 · 칭호 · 문구 · 분류 · 등급 · 조건 · 판정식 · 숨김 · 플레이버 · 필요한 데이터 · 지금 구현 가능 · 출처 · 메모

쓰는 것
  · app/src/main/java/com/slayde/hasenheide/data/AchievementData.kt  — 손으로 고치지 않는다
  · 판정(조건을 채웠나)은 손으로 쓴 data/Achievements.kt 의 업적판정법 에 있다 — 판정식 열은 주석으로만 옮긴다
"""
import csv
import os
import sys

뿌리 = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
원본 = os.path.join(뿌리, "tools", "업적표", "업적표.csv")
결과 = os.path.join(뿌리, "app", "src", "main", "java", "com", "slayde", "hasenheide", "data", "AchievementData.kt")

히든등급 = "(히든 · 등급 없음)"


def 글(s):
    """코틀린 문자열 — 따옴표 · 역슬래시 · $ 를 막고, 줄바꿈은 한 칸으로"""
    s = str(s).replace("\r", " ").replace("\n", " ").strip()
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'


def 주석(s):
    return str(s).replace("\r", " ").replace("\n", " ").replace("*/", "* /").strip()


def 읽기():
    with open(원본, encoding="utf-8") as f:
        줄들 = list(csv.DictReader(f))
    번호들 = [r["번호"] for r in 줄들]
    if len(set(번호들)) != len(번호들):
        sys.exit("번호가 겹친다")
    for r in 줄들:
        a, n = r["번호"].split("-")
        b, m = r["칭호번호"].split("-")
        # 짝: 1-n → 3-n · 2-n → 4-n (12-10 번호판)
        if n != m or {"1": "3", "2": "4"}[a] != b:
            sys.exit("짝 번호가 틀렸다: " + r["번호"] + " → " + r["칭호번호"])
        if (r["숨김"] == "예") != (a == "2"):
            sys.exit("숨김 표시가 번호와 다르다: " + r["번호"])
        if r["지금 구현 가능"] not in ("예", "새 칸", "나중"):
            sys.exit("구현 가능 값이 이상하다: " + r["번호"])
    return 줄들


def 만들기(줄들):
    out = []
    out.append("// ⚠ 자동으로 만든 파일 — 손으로 고치지 않는다.")
    out.append("// 만든 것: tools/업적표_생성.py  ·  원본: tools/업적표/업적표.csv (claude.ai 프로젝트 문서 12-10 번호판 · 스탯명세)")
    out.append("// 업적을 바꾸면: CSV 고치기 → python3 tools/업적표_생성.py → 시험 → 커밋. 판정은 data/Achievements.kt (손으로 쓴 것)")
    out.append("package com.slayde.hasenheide.data")
    out.append("")
    out.append("/** 업적 107개 — 일반 1-n (칭호 3-n) · 히든 2-n (칭호 4-n). 순서는 번호판 그대로 */")
    out.append("object 업적표 {")
    out.append("    val 목록: List<업적> = listOf(")
    for r in 줄들:
        등급 = "" if r["등급"] == 히든등급 else r["등급"]
        out.append("        // 판정식: " + 주석(r["판정식"]))
        if r["메모"].strip():
            out.append("        // 메모: " + 주석(r["메모"]))
        out.append("        업적(" + ", ".join([
            글(r["번호"]), 글(r["이름"]), 글(r["칭호번호"]), 글(r["칭호"]), 글(r["문구"]),
            글(r["분류"]), 글(등급), "true" if r["숨김"] == "예" else "false",
            글(r["조건"]), 글(r["플레이버"]), 글(r["지금 구현 가능"]),
        ]) + "),")
    out.append("    )")
    out.append("")
    out.append("    private val 번호로 = 목록.associateBy { it.번호 }")
    out.append("    private val 칭호로 = 목록.associateBy { it.칭호번호 }")
    out.append("    fun 찾기(번호: String): 업적? = 번호로[번호]")
    out.append("    /** 칭호 번호(3-n · 4-n) 로 찾기 — 대표 칭호 */")
    out.append("    fun 칭호찾기(칭호번호: String): 업적? = 칭호로[칭호번호]")
    out.append("}")
    out.append("")
    return "\n".join(out)


if __name__ == "__main__":
    줄들 = 읽기()
    글자 = 만들기(줄들)
    with open(결과, "w", encoding="utf-8") as f:
        f.write(글자)
    print("업적 %d 개 → %s" % (len(줄들), os.path.relpath(결과, 뿌리)))
