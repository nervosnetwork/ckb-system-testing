UNAME := $(shell uname -s)
# install dependent services or CLIs
setup:
	@echo "[setup] install dependent services or CLI"
	# install Maven
	# for Mac
	@if [ "$(UNAME)" == "Darwin" ]; then\
	  which mvn || brew install maven; \
	else\
	  which mvn || sudo apt-get install maven -y; \
	fi
	
	# show Maven version
	mvn --version

	# install ckb-sdk-java
	if [ "$${CKB_SDK_JAVA_DOWNLOAD_URL}" == "" ]; then \
		make install-sdk ;\
	else \
		make install-sdk-for-ci ;\
	fi

# Download .jar from https://github.com/nervosnetwork/ckb-sdk-java/releases/download/$$CKB_SDK_JAVA_VERSION
#refs: https://gist.github.com/timmolderez/92bea7cc90201cd3273a07cf21d119eb
# The name of your .jar has to follow this naming convention: artifactId-version.jar
# install jar file to repository-location/groupId/artifactId/version
# e.g.: libs/org/nervos/ckb/ckb-sdk-java/0.9.0/ckb-sdk-java-0.9.0.jar
install-sdk:
	@echo "[install-sdk] Download and install Java SDK jar" ;\
	CKB_SDK_JAVA_VERSION="`cat pom.xml | grep -A 1 ">ckb-sdk-java<" | grep version |awk -F "[<>]" '{print $$3}'`" ;\
	CKB_SDK_JAVA_JAR_DIR="libs/org/nervos/ckb/ckb-sdk-java/$$CKB_SDK_JAVA_VERSION" ;\
	CKB_SDK_JAVA_JAR_FILE_NAME="ckb-sdk-java-$$CKB_SDK_JAVA_VERSION.jar" ;\
	CKB_SDK_JAVA_JAR_FILE_PATH="$$CKB_SDK_JAVA_JAR_DIR/$$CKB_SDK_JAVA_JAR_FILE_NAME " ;\
	CKB_SDK_JAVA_POM_TEMPLATE_FILE_PATH="libs/ckb-sdk-java-VERSION.pom" ;\
	CKB_SDK_JAVA_POM_FILE_PATH="$$CKB_SDK_JAVA_JAR_DIR/ckb-sdk-java-$$CKB_SDK_JAVA_VERSION.pom" ;\
	mkdir -p "$$CKB_SDK_JAVA_JAR_DIR" ;\
	test -e $$CKB_SDK_JAVA_JAR_FILE_PATH || curl -L -o $$CKB_SDK_JAVA_JAR_FILE_PATH https://github.com/nervosnetwork/ckb-sdk-java/releases/download/v$$CKB_SDK_JAVA_VERSION/console-$$CKB_SDK_JAVA_VERSION-all.jar ;\
	ls -lah $$CKB_SDK_JAVA_JAR_FILE_PATH ;\
	if [ ! -f $$CKB_SDK_JAVA_POM_FILE_PATH ]; then \
		echo "Create .pom file" ;\
		cp $$CKB_SDK_JAVA_POM_TEMPLATE_FILE_PATH $$CKB_SDK_JAVA_POM_FILE_PATH ;\
		ex -sc "%s/VERSION/$$CKB_SDK_JAVA_VERSION/|x" $$CKB_SDK_JAVA_POM_FILE_PATH ;\
	fi;

	@echo "Install SDK package"
	mvn clean install -DskipTests
	@echo "You should see BUILD SUCCESS"

# Download .jar from CI_URL$$CKB_SDK_JAVA_VERSION
#refs: https://gist.github.com/timmolderez/92bea7cc90201cd3273a07cf21d119eb
# The name of your .jar has to follow this naming convention: artifactId-version.jar
# install jar file to repository-location/groupId/artifactId/version
# e.g.: libs/org/nervos/ckb/ckb-sdk-java/0.12.0_874e642/ckb-sdk-java-0.12.0_874e642.jar
install-sdk-for-ci:
	@echo "[install-sdk-for-ci] Download and install Java SDK jar" ;\
	CKB_SDK_JAVA_DOWNLOAD_URL=${CKB_SDK_JAVA_DOWNLOAD_URL} ;\
	CKB_SDK_JAVA_VERSION="`cat pom.xml | grep -A 1 ">ckb-sdk-java<" | grep version |awk -F "[<>]" '{print $$3}'`" ;\
	CKB_SDK_JAVA_JAR_DIR="libs/org/nervos/ckb/ckb-sdk-java/$$CKB_SDK_JAVA_VERSION" ;\
	CKB_SDK_JAVA_JAR_FILE_NAME="ckb-sdk-java-$$CKB_SDK_JAVA_VERSION.jar" ;\
	CKB_SDK_JAVA_JAR_FILE_PATH="$$CKB_SDK_JAVA_JAR_DIR/$$CKB_SDK_JAVA_JAR_FILE_NAME " ;\
	CKB_SDK_JAVA_POM_TEMPLATE_FILE_PATH="libs/ckb-sdk-java-VERSION.pom" ;\
	CKB_SDK_JAVA_POM_FILE_PATH="$$CKB_SDK_JAVA_JAR_DIR/ckb-sdk-java-$$CKB_SDK_JAVA_VERSION.pom" ;\
	mkdir -p "$$CKB_SDK_JAVA_JAR_DIR" ;\
	test -e $$CKB_SDK_JAVA_JAR_FILE_PATH || curl -L -o $$CKB_SDK_JAVA_JAR_FILE_PATH $$CKB_SDK_JAVA_DOWNLOAD_URL/console-$$CKB_SDK_JAVA_VERSION-all.jar ;\
	ls -lah $$CKB_SDK_JAVA_JAR_FILE_PATH ;\
	if [ ! -f $$CKB_SDK_JAVA_POM_FILE_PATH ]; then \
		echo "Create .pom file" ;\
		cp $$CKB_SDK_JAVA_POM_TEMPLATE_FILE_PATH $$CKB_SDK_JAVA_POM_FILE_PATH ;\
		ex -sc "%s/VERSION/$$CKB_SDK_JAVA_VERSION/|x" $$CKB_SDK_JAVA_POM_FILE_PATH ;\
	fi;

	@echo "Install SDK package"
	mvn clean install -U -DskipTests
	@echo "You should see BUILD SUCCESS"

# run tests with Maven, support CKB_DOCKER_IMAGE_TAG_NAME env
test:
	@echo "[test] run regression tests"
	make update
	# run tests and generate test report
	mvn clean test

test-smoke:
	@echo "[test] run smoke tests, to ensure the most important functions work"
	make update
	# run tests and generate test report
	mvn clean test -DsuiteXmlFile=smokeTest.xml

test-performance:
	@echo "[test] run performance tests"
	make update
	# run tests and generate test report
	mvn clean test -DsuiteXmlFile=performance.xml

test-load:
	@echo "[test] run Load Test"
	make update
	# run tests and generate test report
	mvn clean test -DsuiteXmlFile=loadTest.xml
	
update:
	@echo "[update] update dependencies"
	# check previously ckb bin version
	make check-bin-version

	# update Docker images used for running tests
	./scripts/ckb-entrypoint.sh update run-ckb $${CKB_DOCKER_IMAGE_TAG_NAME:=latest} 8114

	# check currently ckb bin version
	make check-bin-version

# check ckb in version, support CKB_DOCKER_IMAGE_TAG_NAME env
# e.g.: `env CKB_DOCKER_IMAGE_TAG_NAME=6ec0717 make check-bin-version`
# ckb 0.9.0 (rc0.9.0-p0-1-g6ec0717 2019-04-11)
check-bin-version:
	./scripts/ckb-entrypoint.sh run test_ckb_version $${CKB_DOCKER_IMAGE_TAG_NAME:=latest} "ckb --version"

# enter a Bash console, support CKB_DOCKER_IMAGE_TAG_NAME env
console:
	@echo "[console] enter a Bash console of container"
	@./scripts/ckb-entrypoint.sh run test_ckb_console $${CKB_DOCKER_IMAGE_TAG_NAME:=latest} bash
