#!/bin/bash

# Force kill all processes on known service ports
# Use this if stop-all.sh doesn't work

echo "=========================================="
echo "Force Killing All Services on Ports"
echo "=========================================="
echo ""

PORTS=(8080 8083 8090 8091 8092 8100 8101 8102 8110 8111 8112 8120 8121 8122)

KILLED=0
FREE=0

for port in "${PORTS[@]}"; do
    pid=$(lsof -ti :$port 2>/dev/null)
    if [ ! -z "$pid" ]; then
        echo "Killing process on port $port (PID: $pid)"
        kill -9 $pid 2>/dev/null
        ((KILLED++))
    else
        echo "Port $port: Already free"
        ((FREE++))
    fi
done

echo ""
echo "Waiting 2 seconds for processes to terminate..."
sleep 2

echo ""
echo "=========================================="
echo "Verification"
echo "=========================================="

STILL_IN_USE=0
for port in "${PORTS[@]}"; do
    if lsof -ti :$port > /dev/null 2>&1; then
        echo "⚠ Port $port is still in use"
        ((STILL_IN_USE++))
    else
        echo "✓ Port $port is free"
    fi
done

echo ""
echo "=========================================="
echo "Summary"
echo "=========================================="
echo "Killed: $KILLED processes"
echo "Already free: $FREE ports"
if [ $STILL_IN_USE -gt 0 ]; then
    echo "⚠ Still in use: $STILL_IN_USE ports"
    echo ""
    echo "You may need to manually kill these processes:"
    for port in "${PORTS[@]}"; do
        pid=$(lsof -ti :$port 2>/dev/null)
        if [ ! -z "$pid" ]; then
            echo "  Port $port: PID $pid"
            echo "    kill -9 $pid"
        fi
    done
else
    echo "✓ All ports are now free!"
fi
echo ""

