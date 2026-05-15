#!/bin/sh
# Claude Code status line script
input=$(cat)

model=$(echo "$input" | jq -r '.model.display_name // "Unknown Model"')
used=$(echo "$input" | jq -r '.context_window.used_percentage // empty')
cwd=$(echo "$input" | jq -r '.workspace.current_dir // .cwd // ""')

# Get git branch, skip optional locks to avoid conflicts
branch=""
if [ -n "$cwd" ]; then
  branch=$(GIT_OPTIONAL_LOCKS=0 git -C "$cwd" symbolic-ref --short HEAD 2>/dev/null)
fi

# Build context segment
ctx_seg=""
if [ -n "$used" ]; then
  ctx_seg=" | ctx:$(printf '%.0f' "$used")%"
fi

# Build git segment
git_seg=""
if [ -n "$branch" ]; then
  git_seg=" | $branch"
fi

printf "%s%s | %s%s" "$model" "$ctx_seg" "$cwd" "$git_seg"
