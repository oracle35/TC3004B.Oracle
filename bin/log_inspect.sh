#!/bin/sh

# Kubernetes Pod Logs tmux Script
# Usage: ./k8s-tmux-logs.sh [namespace] [label-selector]
# Examples:
#   ./k8s-tmux-logs.sh                    # All pods in default namespace
#   ./k8s-tmux-logs.sh kube-system        # All pods in kube-system namespace
#   ./k8s-tmux-logs.sh default app=nginx  # Pods with label app=nginx in default namespace

set -e

# Configuration
NAMESPACE="${1:-default}"
LABEL_SELECTOR="${2:-}"
SESSION_NAME="k8s-logs-$(date +%s)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting Kubernetes Pod Logs tmux Session${NC}"
echo -e "Namespace: ${YELLOW}$NAMESPACE${NC}"
if [[ -n "$LABEL_SELECTOR" ]]; then
    echo -e "Label Selector: ${YELLOW}$LABEL_SELECTOR${NC}"
fi

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}Error: kubectl is not installed or not in PATH${NC}"
    exit 1
fi

# Check if tmux is available
if ! command -v tmux &> /dev/null; then
    echo -e "${RED}Error: tmux is not installed or not in PATH${NC}"
    exit 1
fi

# Build kubectl command
KUBECTL_CMD="kubectl get pods -n $NAMESPACE"
if [[ -n "$LABEL_SELECTOR" ]]; then
    KUBECTL_CMD="$KUBECTL_CMD -l $LABEL_SELECTOR"
fi

# Get list of running pods
echo -e "${YELLOW}Fetching pods...${NC}"
PODS=$($KUBECTL_CMD --field-selector=status.phase=Running -o jsonpath='{.items[*].metadata.name}')

if [[ -z "$PODS" ]]; then
    echo -e "${RED}No running pods found in namespace '$NAMESPACE'${NC}"
    if [[ -n "$LABEL_SELECTOR" ]]; then
        echo -e "${RED}with label selector '$LABEL_SELECTOR'${NC}"
    fi
    exit 1
fi

# Convert space-separated string to array
POD_ARRAY=($PODS)
POD_COUNT=${#POD_ARRAY[@]}

echo -e "${GREEN}Found $POD_COUNT running pod(s)${NC}"

# Kill existing session if it exists
tmux kill-session -t "$SESSION_NAME" 2>/dev/null || true

# Get tmux configuration
PREFIX_KEY=$(tmux show-options -g prefix | cut -d' ' -f2)
BASE_INDEX=$(tmux show-options -g base-index | cut -d' ' -f2 2>/dev/null || echo "0")

# Create new tmux session
echo -e "${YELLOW}Creating tmux session: $SESSION_NAME${NC}"
tmux new-session -d -s "$SESSION_NAME"

# Get the actual first window index after session creation
FIRST_WINDOW=$(tmux list-windows -t "$SESSION_NAME" -F "#{window_index}" | head -n1)

# Create a window for each pod
WINDOWS_CREATED=()
for i in "${!POD_ARRAY[@]}"; do
    POD_NAME="${POD_ARRAY[$i]}"
    WINDOW_NAME="$((i+1))-${POD_NAME:0:20}"  # Limit window name length
    
    if [[ $i -eq 0 ]]; then
        # Rename the first window
        tmux rename-window -t "$SESSION_NAME:$FIRST_WINDOW" "$WINDOW_NAME"
        tmux send-keys -t "$SESSION_NAME:$FIRST_WINDOW" "kubectl logs -n $NAMESPACE -f $POD_NAME" Enter
        WINDOWS_CREATED+=("$FIRST_WINDOW")
    else
        # Create new window for subsequent pods
        tmux new-window -t "$SESSION_NAME" -n "$WINDOW_NAME"
        # Get the index of the window we just created
        CURRENT_WINDOW=$(tmux list-windows -t "$SESSION_NAME" -F "#{window_index}" | tail -n1)
        tmux send-keys -t "$SESSION_NAME:$CURRENT_WINDOW" "kubectl logs -n $NAMESPACE -f $POD_NAME" Enter
        WINDOWS_CREATED+=("$CURRENT_WINDOW")
    fi
    
    echo -e "  ${GREEN}✓${NC} Window created for pod: ${YELLOW}$POD_NAME${NC}"
done

# Create a summary window
tmux new-window -t "$SESSION_NAME" -n "summary"
SUMMARY_WINDOW=$(tmux list-windows -t "$SESSION_NAME" -F "#{window_index}" | tail -n1)
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "clear" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo '=== Kubernetes Pod Logs Summary ==='" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo 'Namespace: $NAMESPACE'" Enter
if [[ -n "$LABEL_SELECTOR" ]]; then
    tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo 'Label Selector: $LABEL_SELECTOR'" Enter
fi
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo 'Total Pods: $POD_COUNT'" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo ''" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo 'Pod List:'" Enter
for POD_NAME in "${POD_ARRAY[@]}"; do
    tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo '  - $POD_NAME'" Enter
done
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo ''" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo 'Navigation:'" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo '  $PREFIX_KEY n  - Next window'" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo '  $PREFIX_KEY p  - Previous window'" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo '  $PREFIX_KEY [0-9] - Go to window number'" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo '  $PREFIX_KEY d  - Detach session'" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo '  Ctrl+c    - Stop log following'" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "echo ''" Enter
tmux send-keys -t "$SESSION_NAME:$SUMMARY_WINDOW" "kubectl get pods -n $NAMESPACE" Enter

# Switch to the first pod window
tmux select-window -t "$SESSION_NAME:${WINDOWS_CREATED[0]}"

echo -e "${GREEN}tmux session '$SESSION_NAME' created successfully!${NC}"
echo -e "${YELLOW}Attaching to session...${NC}"
echo -e "Use ${YELLOW}$PREFIX_KEY d${NC} to detach, ${YELLOW}tmux attach -t $SESSION_NAME${NC} to reattach"

# Attach to the session
tmux attach-session -t "$SESSION_NAME"
