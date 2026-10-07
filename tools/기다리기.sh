#!/bin/bash
# GitHub 검사가 끝날 때까지 20초마다 본다 — 끝나면 바로 결과를 찍고 나간다 (10-08: 정해진 시간 sleep 금지)
# 쓰는 법: tools/기다리기.sh <가지이름> [push 한 커밋 앞 7자리]   (예: tools/기다리기.sh main $(git rev-parse --short=7 HEAD))
# ⚠ bash 변수 이름은 영문만 된다
BR=${1:-main}; SHA=${2:-}
API=https://api.github.com/repos/slayde3470/gymwork-app
for i in $(seq 1 60); do
  LINE=$(curl -sS "$API/actions/runs?branch=$BR&per_page=1" | python3 -c "
import json,sys
try:
  r=json.load(sys.stdin)['workflow_runs'][0]; print(r['id'],r['status'],r['conclusion'],r['head_sha'][:7])
except Exception: print('- - - -')")
  set -- $LINE
  if [ -n "$SHA" ] && [ "$4" != "$SHA" ]; then sleep 10; continue; fi   # 아직 새 run 이 안 생김
  if [ "$2" = completed ]; then
    echo "끝: run $1 · $4 · $3"
    if [ "$3" != success ]; then
      J=$(curl -sS "$API/actions/runs/$1/jobs" | python3 -c "import json,sys;print(json.load(sys.stdin)['jobs'][0]['id'])")
      curl -sS "$API/check-runs/$J/annotations" | python3 -c "import json,sys;[print(a['message'][:300]) for a in json.load(sys.stdin) if 'Node.js' not in a['message']][:8]"
    fi
    exit 0
  fi
  sleep 20
done
echo "20분이 지나도 안 끝남 — run $1"
