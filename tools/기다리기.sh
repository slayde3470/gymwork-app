#!/bin/bash
# GitHub 검사가 끝날 때까지 20초마다 본다 — 끝나면 바로 결과를 찍고 나간다 (10-08: 정해진 시간 sleep 금지)
# 쓰는 법: tools/기다리기.sh <가지이름> [push 한 커밋 앞 7자리]   (예: tools/기다리기.sh main $(git rev-parse --short=7 HEAD))
# ⚠ bash 변수 이름은 영문만 된다
BR=${1:-main}; SHA=${2:-}
API=https://api.github.com/repos/slayde3470/gymwork-app
for i in $(seq 1 60); do
  LINE=$(curl -sS "$API/actions/runs?branch=$BR&per_page=1" | python3 -c "
import json,sys
from datetime import datetime
try:
  r=json.load(sys.stdin)['workflow_runs'][0]
  t=lambda x: datetime.strptime(x,'%Y-%m-%dT%H:%M:%SZ')
  secs=int((t(r['updated_at'])-t(r['run_started_at'])).total_seconds())
  print(r['id'],r['status'],r['conclusion'],r['head_sha'][:7],secs)
except Exception: print('- - - - 0')")
  set -- $LINE
  if [ -n "$SHA" ] && [ "$4" != "$SHA" ]; then
    # 10-09: 바뀐 파일이 없는 커밋(빈 커밋 · app/screens 만 바뀜)은 판을 부르지 않는다 — 2분 넘게 안 생기면 알리고 나간다 (그때 20분을 헛기다렸다)
    NEW=$((NEW+1)); if [ "$NEW" -ge 12 ]; then echo "⚠ 2분이 지나도 $SHA 판이 안 생김 — 빈 커밋이거나 판을 부르지 않는 파일만 바뀐 것. 실제 파일을 바꿔 다시 올린다"; exit 1; fi
    sleep 10; continue
  fi
  if [ "$2" = completed ]; then
    echo "끝: run $1 · $4 · $3 · ${5}초"
    # 10-09: 사진 판이 1분 안에 '성공'으로 끝나면 사진을 안 찍은 것이다 (찍을 화면 고르기에서 바로 멈춘 경우) — 실패로 본다
    case "$BR" in screens/*) if [ "$3" = success ] && [ "${5:-0}" -lt 60 ]; then
      echo "⚠ 사진 판이 ${5}초 만에 끝남 — 사진을 안 찍었다. 실패로 본다 (찍을것.txt · screens.yml 확인)"; exit 1; fi;; esac
    if [ "$3" != success ]; then
      J=$(curl -sS "$API/actions/runs/$1/jobs" | python3 -c "import json,sys;print(json.load(sys.stdin)['jobs'][0]['id'])")
      curl -sS "$API/check-runs/$J/annotations" | python3 -c "import json,sys;[print(a['message'][:300]) for a in json.load(sys.stdin) if 'Node.js' not in a['message']][:8]"
    fi
    exit 0
  fi
  sleep 20
done
echo "20분이 지나도 안 끝남 — run $1"
