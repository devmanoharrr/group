SHELL := /bin/bash
MVN := ./mvnw -q

.PHONY: help verify check format test install clean

help:
	@echo "make verify   - format check + checkstyle (no tests)"
	@echo "make check    - spotless:check + checkstyle:check"
	@echo "make format   - apply formatting (spotless:apply)"
	@echo "make test     - run tests"
	@echo "make install  - clean install (skip tests)"
	@echo "make clean    - clean build artifacts"

verify:
	$(MVN) -DskipTests clean verify

check:
	$(MVN) spotless:check checkstyle:check

format:
	$(MVN) spotless:apply

test:
	./mvnw -q test

install:
	$(MVN) -DskipTests clean install

clean:
	./mvnw -q clean
