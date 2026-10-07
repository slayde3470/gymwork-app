#!/bin/bash
# GitHub 검사가 끝날 때까지 20초마다 본다 — 끝나면 바로 결과를 찍고 나간다 (10-08: 정해진 시간 sleep 금지)
# 쓰는 법: tools/기다리기.sh <가지이름>   (예: main · screens/now)
가지=${1:-main}
API=https://api.github.com/repos/slayde3470/gymwork-app
sleep 15   # push 직후 run 이 생길 때까지
for i in $(seq 1 60); do
  줄=$(curl -sS "$API/actions/runs?branch=$가지&per_page=1" | python3 -c "import json,sys;r=json.load(sys.stdin)['workflow_runs'][0];print(r['id'],r['status'],r['conclusion'],r['head_sha'][:7])")
  set -- $줄
  if [ "$2" = completed ]; then
    echo "끝: run $1 · $4 · $3"
    if [ "$3" != success ]; then
      j=$(curl -sS "$API/actions/runs/$1/jobs" | python3 -c "import json,sys;print(json.load(sys.stdin)['jobs'][0]['id'])")
      curl -sS "$API/check-runs/$j/annotations" | python3 -c "import json,sys;[print(a['message'][:300]) for a in json.load(sys.stdin) if 'Node.js' not in a['message']][:8]"
    fi
    exit 0
  fi
  sleep 20
done
echo "20분이 지나도 안 끝남 — run $1"
