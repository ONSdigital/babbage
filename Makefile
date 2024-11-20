NVM_SOURCE_PATH ?= $(HOME)/.nvm/nvm.sh

ifneq ("$(wildcard $(NVM_SOURCE_PATH))","")
	NVM_EXEC = source $(NVM_SOURCE_PATH) && nvm exec --
endif
NPM = $(NVM_EXEC) npm

.PHONY: all
all: audit test build

.PHONY: audit
audit : audit-java audit-js

.PHONY: audit-java
audit-java:
	mvn ossindex:audit

.PHONY: audit-js
audit-js:
	$(NPM) audit --prefix src/main/web --audit-level=high

.PHONY: build
build:
	$(NPM) install --prefix src/main/web --unsafe-perm
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