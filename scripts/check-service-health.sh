#!/bin/bash

# =============================================================================
# check-service-health.sh - Check if a service is healthy
# =============================================================================
#
# Usage: check-service-health.sh <url> <timeout_seconds>
# Returns: 0 if healthy, 1 if not
#

URL=$1
TIMEOUT=${2:-10}

# Try to connect to the service
if command -v curl &> /dev/null; then
    if curl -s -f --max-time $TIMEOUT "$URL" > /dev/null 2>&1; then
        return 0
    fi
elif command -v wget &> /dev/null; then
    if wget -q --timeout=$TIMEOUT --spider "$URL" > /dev/null 2>&1; then
        return 0
    fi
fi

return 1

