#!/usr/bin/env bash

# Multi-Agent Orchestrator
# Coordinates the feature development lifecycle

set -e

PROJECT_DIR="/Users/mahibrahim/Projects/DailyReminder"
cd "$PROJECT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Banner
clear
echo "╔════════════════════════════════════════════════╗"
echo "║                                                ║"
echo "║     🤖 MULTI-AGENT ORCHESTRATOR 🤖            ║"
echo "║     Daily Work Tracker Development System     ║"
echo "║                                                ║"
echo "╚════════════════════════════════════════════════╝"
echo ""

# Parse command
COMMAND="${1:-help}"
FEATURE_NAME="${2:-}"

case "$COMMAND" in
  "suggest")
    echo -e "${BLUE}═══════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  PHASE 1: FEATURE SUGGESTION${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════${NC}"
    echo ""
    chmod +x agents/feature-suggester.sh
    ./agents/feature-suggester.sh
    echo ""
    echo -e "${GREEN}✅ Suggestion phase complete${NC}"
    echo ""
    echo "Next step: Review suggestions and choose a feature"
    echo "Then run: ./agents/orchestrator.sh plan <feature-name>"
    ;;

  "plan")
    if [ -z "$FEATURE_NAME" ]; then
        echo -e "${RED}❌ Error: Feature name required${NC}"
        echo ""
        echo "Usage: ./agents/orchestrator.sh plan <feature-name>"
        echo ""
        echo "Available features:"
        echo "  - search-filter"
        echo "  - time-tracking"
        echo "  - export"
        exit 1
    fi

    echo -e "${BLUE}═══════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  PHASE 2: IMPLEMENTATION PLANNING${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════${NC}"
    echo ""
    chmod +x agents/feature-implementer.sh
    ./agents/feature-implementer.sh "$FEATURE_NAME"
    echo ""
    echo -e "${GREEN}✅ Planning phase complete${NC}"
    echo ""
    echo "Next step: Implement the feature according to the plan"
    echo "Then run: ./agents/orchestrator.sh verify $FEATURE_NAME"
    ;;

  "verify")
    if [ -z "$FEATURE_NAME" ]; then
        FEATURE_NAME="latest"
    fi

    echo -e "${BLUE}═══════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  PHASE 3: FEATURE VERIFICATION${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════${NC}"
    echo ""
    chmod +x agents/feature-verifier.sh
    ./agents/feature-verifier.sh "$FEATURE_NAME"
    echo ""
    echo -e "${GREEN}✅ Verification phase complete${NC}"
    echo ""
    echo "Feature is ready! 🎉"
    ;;

  "full-cycle")
    if [ -z "$FEATURE_NAME" ]; then
        echo -e "${RED}❌ Error: Feature name required for full cycle${NC}"
        echo ""
        echo "Usage: ./agents/orchestrator.sh full-cycle <feature-name>"
        exit 1
    fi

    echo -e "${YELLOW}═══════════════════════════════════════════════${NC}"
    echo -e "${YELLOW}  FULL DEVELOPMENT CYCLE${NC}"
    echo -e "${YELLOW}═══════════════════════════════════════════════${NC}"
    echo ""

    # Phase 1: Suggest
    echo "Phase 1/3: Feature Suggestion"
    ./agents/orchestrator.sh suggest
    echo ""
    read -p "Press Enter to continue to planning..."
    echo ""

    # Phase 2: Plan
    echo "Phase 2/3: Implementation Planning"
    ./agents/orchestrator.sh plan "$FEATURE_NAME"
    echo ""
    echo -e "${YELLOW}⚠️  MANUAL STEP REQUIRED${NC}"
    echo "Please implement the feature according to the plan."
    echo ""
    read -p "Press Enter when implementation is complete..."
    echo ""

    # Phase 3: Verify
    echo "Phase 3/3: Verification"
    ./agents/orchestrator.sh verify "$FEATURE_NAME"
    echo ""
    echo -e "${GREEN}╔════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                                                ║${NC}"
    echo -e "${GREEN}║       🎉 FULL CYCLE COMPLETE! 🎉              ║${NC}"
    echo -e "${GREEN}║                                                ║${NC}"
    echo -e "${GREEN}╚════════════════════════════════════════════════╝${NC}"
    ;;

  "status")
    echo -e "${BLUE}═══════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  SYSTEM STATUS${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════${NC}"
    echo ""

    echo "📂 Project: Daily Work Tracker"
    echo "📍 Location: $PROJECT_DIR"
    echo ""

    echo "🤖 Available Agents:"
    echo "   ✓ Feature Suggester"
    echo "   ✓ Feature Implementer"
    echo "   ✓ Feature Verifier"
    echo ""

    echo "📊 Recent Activity:"
    if [ -d "agents" ]; then
        echo "   Suggestion files: $(find agents -name "feature-suggestions.txt" 2>/dev/null | wc -l | tr -d ' ')"
        echo "   Implementation plans: $(find agents -name "implementation-plan-*.md" 2>/dev/null | wc -l | tr -d ' ')"
        echo "   Verification reports: $(find agents -name "verification-report-*.txt" 2>/dev/null | wc -l | tr -d ' ')"
    else
        echo "   No activity yet"
    fi
    echo ""

    echo "🔧 Build Status:"
    if ./gradlew build --quiet 2>/dev/null; then
        echo "   ✅ Build: PASSING"
    else
        echo "   ❌ Build: FAILING"
    fi
    echo ""
    ;;

  "help"|*)
    echo "Multi-Agent Orchestrator"
    echo ""
    echo "Usage:"
    echo "  ./agents/orchestrator.sh <command> [feature-name]"
    echo ""
    echo "Commands:"
    echo "  suggest              - Run feature suggestion agent"
    echo "  plan <feature>       - Plan feature implementation"
    echo "  verify [feature]     - Verify implemented feature"
    echo "  full-cycle <feature> - Run complete development cycle"
    echo "  status              - Show system status"
    echo "  help                - Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./agents/orchestrator.sh suggest"
    echo "  ./agents/orchestrator.sh plan search-filter"
    echo "  ./agents/orchestrator.sh verify search-filter"
    echo "  ./agents/orchestrator.sh full-cycle export"
    echo ""
    echo "Agents:"
    echo "  🤖 Feature Suggester  - Analyzes app and suggests features"
    echo "  🔨 Feature Implementer - Creates implementation plans"
    echo "  ✅ Feature Verifier   - Tests and validates features"
    echo ""
    ;;
esac
