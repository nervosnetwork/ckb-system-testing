# How to write tests

## source code for test cases

[src/test/java/org.nervos.ckb](src/test/java/org.nervos.ckb)


## create a new test case

read [doc](https://www.yiibai.com/testng/configuration-annotations.html) for more details.


## include new test case to TestNG config file

for example:

```
<suite name="CKB System Testing" parallel="tests" thread-count="1">
    <test name="JSONRPC API Tests" preserve-order="true" verbose="2">
         <classes>
             <class name="org.nervos.ckb.function.jsonrpcapicase.BlockNumber"/>
         </classes>
    </test/>
</suite>
```

## TestNG config file

[systemTest.xml](systemTest.xml)

The XML file is using to config the Test Suite name and test cases for each Test Group.

You can also define new groups inside XML and specify additional details in attributes, such as whether to run the tests in parallel, how many threads to use, whether you are running JUnit tests, etc... 

By default, TestNG will run your tests in the order they are found in the XML file. If you want the classes and methods listed in this file to be run in an unpredictable order, set the preserve-order attribute to false.

Read [doc](http://testng.org/doc/documentation-main.html#testng-xml) for more details about config file. 


## Refs

* http://testng.org/doc/documentation-main.html
* https://www.yiibai.com/testng/
* https://www.tutorialspoint.com/testng/index.htm
* https://www.tutorialspoint.com/testng/testng_quick_guide.htm

### Assertions

* https://static.javadoc.io/org.testng/testng/6.9.10/org/testng/Assert.html
* https://github.com/junit-team/junit4/wiki/Assertions
* https://objectpartners.com/2013/09/18/the-benefits-of-using-assertthat-over-other-assert-methods-in-unit-tests/
