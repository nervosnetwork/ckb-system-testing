# How to run tests


We use `Maven` as the project build tool(also dependency management) for this project.


# About Maven

## What is Maven

> Apache Maven 是一套软件工程管理和整合工具。基于工程对象模型（POM）的概念，通过一个中央信息管理模块，Maven 能够管理项目的构建、报告和文档。

read more about Maven: http://wiki.jikexueyuan.com/project/maven/


## Install Maven

### for Mac

```
$ brew install maven
```

check, for example:

```
$ mvn --version                                                                                                                                      Sun Sep 23 17:37:43 CST 2018
Apache Maven 3.5.4 (1edded0938998edf8bf061f1ceb3cfdeccf443fe; 2018-06-18T02:33:14+08:00)
Maven home: /usr/local/Cellar/maven/3.5.4/libexec
Java version: 9, vendor: Oracle Corporation, runtime: /Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home
Default locale: en_CN, platform encoding: UTF-8
OS name: "mac os x", version: "10.12.6", arch: "x86_64", family: "mac"
```


## What is pom.xml：

Maven is using `pom.xml` file to config Java package dependencies and config plugins.

> pom.xml主要描述了项目的maven坐标，依赖关系，开发者需要遵循的规则，缺陷管理系统，组织和licenses，以及其他所有的项目相关因素，是项目级别的配置文件。

read more about pom.xml: https://blog.csdn.net/u012152619/article/details/51485297


## Run tests

### Config enviroment variable

We setup a enviroment variable named `CHAIN_URL` as testing target in each test case, you should change it to your chain before running the tests.

```
CKB_CHAIN_URL=http://127.0.0.1:8114
```

**this chain is used for local testing purpose, MUST NOT use this chain on CI enviroment**

set environment variable for IDEA on Mac OS:

```
launchctl setenv CKB_CHAIN_URL http://127.0.0.1:8114
# NOTE: MUST restart IDEA after changing the environment variable
```

check:

```
launchctl getenv CKB_CHAIN_URL
```

### Run tests Via Terminal

```
$ mvn test
```

should see output like:

```
[INFO] Scanning for projects...
[INFO]
[INFO] ----------------------< org.nervos.ckb:systemTest >-----------------------
[INFO] Building systemTest 0.0.1
[INFO] --------------------------------[ jar ]---------------------------------
...

[INFO]
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running TestSuite

...

result:success
[TestNG] Time taken by org.testng.reporters.XMLReporter@78aab498: 109 ms
[TestNG] Time taken by org.testng.reporters.jq.Main@318ba8c8: 133 ms
[TestNG] Time taken by org.testng.reporters.JUnitReportReporter@6d2a209c: 35 ms
[TestNG] Time taken by org.testng.reporters.EmailableReporter2@1677d1: 13 ms
[TestNG] Time taken by org.testng.reporters.SuiteHTMLReporter@4df50bcc: 32 ms
[TestNG] Time taken by [FailedReporter passed=0 failed=0 skipped=0]: 4 ms
[INFO] Tests run: 200, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 324.052 s - in TestSuite
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 200, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 05:42 min
[INFO] Finished at: 2018-09-23T17:46:09+08:00
[INFO] ------------------------------------------------------------------------
```

* run tests for a specified suite XML file

```
# run smoke tests
mvn test -DsuiteXmlFile=smokeTest.xml

# run performance tests
mvn test -DsuiteXmlFile=performance.xml

```

* run tests for a class:

```
mvn test -Dtest=[ClassName]

# example
mvn test -Dtest=org.nervos.ckb.jsonrpcTest.GetTipBlockNumberTest
```

* run tests for a method:

```
mvn test -Dtest=[ClassName]#[MethodName]

# example
mvn test -Dtest=org.nervos.ckb.jsonrpcTest.GetTipBlockNumberTest#testGetTipBlockNumberNegative
```

* run tests using wildcard `*`:

```
mvn test -Dtest=MyClassTest#\*test\*

# example
mvn test -Dtest=\*jsonrpcTest.GetTipBlockNumberTest
mvn test -Dtest=\*jsonrpcTest.GetTipBlockNumberTest#\*Negative
```


### Via IDE

take `IntelliJ IDEA` for example:

* run all tests:

    open the project with IDE and right click on the file `systemText.xml` in the navigation area and select `Run '.../systemText.xml'`

* run a sets of tests:

    open the project with IDE and right click on the class file, e.g. `src/test/java/com/cryptape/jsonrpcTest/GetTipBlockNumberTest.java` in the navigation area and select `Run BlockNumber`

* run a single test case:

    open the project with IDE and open the class file, e.g. `src/test/java/com/cryptape/jsonrpcTest/GetTipBlockNumberTest.java`, click on the "Run Test" icon beside the method `public void testGetTipBlockNumberPositive` and select `Run 'testGetTipBlockNumberPositive()'`

## Test Result Report

after running the tests, `maven-surefire-plugin` plugin will auto genreate a report which saved at:

`target/surefire-reports/html/index.html`


## Todo:

* use a better report generator to replace `maven-surefire-plugin`

## Refs

* [A Simple 12 Steps Guide to Write an Effective Test Summary Report](https://www.softwaretestinghelp.com/test-summary-report-template-download-sample/)

