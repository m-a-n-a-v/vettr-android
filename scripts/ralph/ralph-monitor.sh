#!/bin/bash
# Ralph Monitor - Auto-restart wrapper that keeps Ralph running until all stories complete
# Usage: ./ralph-monitor.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PRD_FILE="$SCRIPT_DIR/prd.json"
MAX_RESTARTS=20
RESTART_COUNT=0

check_progress() {
    python3 -c "
import json
d=json.load(open('$PRD_FILE'))
stories=d['userStories']
passed=sum(1 for s in stories if s.get('passes'))
remaining=len(stories)-passed
print(f'{passed}/{len(stories)}')
" 2>/dev/null
}

while [ $RESTART_COUNT -lt $MAX_RESTARTS ]; do
    PROGRESS=$(check_progress)
    PASSED=$(echo "$PROGRESS" | cut -d/ -f1)
    TOTAL=$(echo "$PROGRESS" | cut -d/ -f2)
    REMAINING=$((TOTAL - PASSED))

    echo ""
    echo "========================================"
    echo "  Ralph Monitor - $(date '+%H:%M:%S')"
    echo "  Progress: $PROGRESS ($REMAINING remaining)"
    echo "  Restart count: $RESTART_COUNT"
    echo "========================================"

    if [ "$REMAINING" -eq 0 ]; then
        echo "ALL STORIES COMPLETE!"
        exit 0
    fi

    ITERATIONS=$((REMAINING + 3))
    echo "Starting Ralph with $ITERATIONS iterations..."

    cd "$SCRIPT_DIR"
    ./ralph.sh --tool claude "$ITERATIONS" || true

    RESTART_COUNT=$((RESTART_COUNT + 1))

    NEW_PROGRESS=$(check_progress)
    if [ "$NEW_PROGRESS" = "$PROGRESS" ]; then
        echo "WARNING: No progress made in this run. Waiting 30s before retry..."
        sleep 30
    else
        echo "Progress made: $PROGRESS -> $NEW_PROGRESS"
        sleep 5
    fi
done

echo "Reached max restarts ($MAX_RESTARTS). Check status manually."
exit 1
