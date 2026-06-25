NVM_SOURCE_PATH ?= $(HOME)/.nvm/nvm.sh

ifneq ("$(wildcard $(NVM_SOURCE_PATH))","")
	NVM_EXEC = source $(NVM_SOURCE_PATH) && nvm exec --
endif
NPM = $(NVM_EXEC) npm

OSSINDEX_ERRORS = "Unable to contact OSS Index|authentication failed|401 Unauthorized|403 Forbidden|429 Too Many Requests|Too many requests|Rate limit|Unknown host|Connection refused|timed out|unreachable|402 Payment Required"

.PHONY: all
all: audit test build

.PHONY: audit
audit : audit-java audit-js

.PHONY: audit-java
audit-java:
	@echo "🔍 Running OSS Index audit..." && \
	mkdir -p target && \
	mvn -B ossindex:audit > target/ossindex-audit.log 2>&1; status=$$?; cat target/ossindex-audit.log; \
	[ $$status -eq 0 ] && grep -Eiqn $(OSSINDEX_ERRORS) target/ossindex-audit.log && \
		{ echo "❌ OSS Index API/auth/network error detected — see target/ossindex-audit.log"; exit 1; }; \
	exit $$status

.PHONY: audit-js
audit-js:
	$(NPM) audit --prefix src/main/web --audit-level=high

.PHONY: build
build:
	$(NPM) install --prefix src/main/web
	mvn -Dmaven.test.skip -Dossindex.skip=true clean package dependency:copy-dependencies

.PHONY: debug-web
debug-web:
	./run.sh

.PHONY: debug-publishing
debug-publishing:
	./run-publishing.sh

.PHONY: test
test:
	mvn -Dossindex.skip=true test
