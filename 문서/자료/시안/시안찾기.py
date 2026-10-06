#!/usr/bin/env python3
"""시안찾기 — 37만 글자 시안(7일체험.html)을 통째로 읽지 않고, 필요한 조각만 뽑는다. (2026-10-06)

쓰는 법 (저장소 맨 위에서):
  python3 문서/자료/시안/시안찾기.py 루틴종목상자            # 그 함수 정의 전부(나중 판이 덮어쓴 것까지, 순서대로)
  python3 문서/자료/시안/시안찾기.py 루틴종목상자 보고칸      # 여러 개 한 번에
  python3 문서/자료/시안/시안찾기.py --css 루추가뜸           # 그 클래스가 나오는 CSS 줄
  python3 문서/자료/시안/시안찾기.py --쓰는곳 루틴종목상자     # 부르는 줄 번호만 (내용은 안 보임)
  python3 문서/자료/시안/시안찾기.py --이름 루틴              # 이름에 '루틴'이 든 함수 목록
  python3 문서/자료/시안/시안찾기.py --판 v22                 # 그 판에 바뀐 것 = patch 파일 목록 (이것만 읽으면 된다)

이번 판에 '무엇이 바뀌었나'는 시안 전체가 아니라 7일체험_만들기/patch_vNN*.py 에 바뀐 곳만 들어 있다.
"""
import re, sys, glob, os

여기 = os.path.dirname(os.path.abspath(__file__))
시안 = os.path.join(여기, "7일체험.html")
줄들 = open(시안, encoding="utf-8").read().split("\n")
최대줄 = 300


def 정의시작(이름):
    e = re.escape(이름)
    p = re.compile(
        rf"(^|[\s;}}])(async\s+)?function\s+{e}\s*\("        # function 이름(
        rf"|(^|[\s;{{}}])(const|let|var)\s+{e}\s*="           # const 이름 =
        rf"|(^|[\s;{{}}]){e}\s*=\s*(function|\(|async|[^=])"   # 이름 = ... (나중 판이 덮어쓰기)
    )
    return [i for i, l in enumerate(줄들) if p.search(l) and not l.lstrip().startswith(("//", "/*", "*"))]


def 몸통(i):
    깊이, 시작됨, 끝 = 0, False, i
    for j in range(i, min(i + 최대줄, len(줄들))):
        l = 줄들[j]
        깊이 += l.count("{") - l.count("}")
        if "{" in l:
            시작됨 = True
        끝 = j
        if (시작됨 and 깊이 <= 0) or (not 시작됨 and l.rstrip().endswith(";")):
            break
    return 끝


def 함수(이름):
    시작들 = 정의시작(이름)
    if not 시작들:
        print(f"== {이름}: 정의 없음 (--이름 {이름[:2]} 로 비슷한 이름을 찾아 보세요)")
        return
    for k, i in enumerate(시작들, 1):
        끝 = 몸통(i)
        print(f"== {이름} [{k}/{len(시작들)}] 줄 {i+1}~{끝+1}")
        for j in range(i, 끝 + 1):
            l = 줄들[j]
            print(l if len(l) <= 1500 else l[:1500] + f" …(이 줄 {len(l)}자, 잘림)")
        print()


def css(이름):
    p = re.compile(r"\." + re.escape(이름) + r"(?![\w가-힣-])")
    for i, l in enumerate(줄들):
        if p.search(l) and "{" in l and ("}" in l) and "${" not in l:
            print(f"{i+1}: {l[:600]}")


def 쓰는곳(이름):
    p = re.compile(r"(?<![\w가-힣])" + re.escape(이름) + r"(?![\w가-힣])")
    정의 = set(정의시작(이름))
    번호 = [str(i + 1) for i, l in enumerate(줄들) if p.search(l) and i not in 정의]
    print(f"{이름} 쓰는 줄 ({len(번호)}곳): " + " ".join(번호))


def 이름목록(조각):
    p = re.compile(r"(?:function\s+|(?:const|let|var)\s+)([\w가-힣]*" + re.escape(조각) + r"[\w가-힣]*)")
    본 = {}
    for i, l in enumerate(줄들):
        for m in p.finditer(l):
            본.setdefault(m.group(1), i + 1)
    for n, i in sorted(본.items(), key=lambda x: x[1]):
        print(f"{i}: {n}")


def 판(v):
    목록 = sorted(glob.glob(os.path.join(여기, "7일체험_만들기", f"patch_{v}*.py")))
    if not 목록:
        print(f"{v}: patch 파일 없음")
    for f in 목록:
        첫 = open(f, encoding="utf-8").read().split('"""')
        설명 = 첫[1].strip().split("\n")[0] if len(첫) > 2 else ""
        print(f"{os.path.relpath(f, os.getcwd())}  ({os.path.getsize(f)//1000}KB)  {설명[:160]}")


if __name__ == "__main__":
    a = sys.argv[1:]
    if not a:
        print(__doc__); sys.exit(0)
    할일 = {"--css": css, "--쓰는곳": 쓰는곳, "--이름": 이름목록, "--판": 판}
    if a[0] in 할일:
        for n in a[1:]:
            할일[a[0]](n)
    else:
        for n in a:
            함수(n)
