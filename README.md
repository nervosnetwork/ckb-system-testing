# CKB System Testing

This project is a [TestNG](https://testng.org/) (Java) project, using for running system tests for [Nervos CKB](https://github.com/nervosnetwork/ckb).

[![Build Status](https://travis-ci.com/nervosnetwork/ckb-system-testing.svg?branch=master)](https://travis-ci.com/nervosnetwork/ckb-system-testing)
![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/nervosnetwork/ckb-system-testing.svg)

## Getting Started

These instructions will guide you how to run and write test cases.

### Prerequisites

What things you need to install the software and how to install them

* Java
* Maven

### Installing

install Java on Mac: https://java.com/en/download/help/mac_install.xml


## Running the tests

### Run regression/smoke/performance tests

```
make test # run regression tests, slow

make test-smoke # run smoke tests, fast

make test-performance # run performance tests, very slow

make test-load # run load tests, very slow, read config from 'loadConfig.yml'
```

Explain how to run the tests, read [docs/how-to-run-tests.md](docs/how-to-run-tests.md) for details.

## Write test cases

Instructions for how to write test cases, read [docs/how-to-write-tests.md](docs/how-to-write-tests.md) for details.


## Coding style

We use "Google Java Style Guide" as coding style guide:

* Official: https://google.github.io/styleguide/javaguide.html
* Chinese version: https://jervyshi.gitbooks.io/google-java-styleguide-zh/content/

### Installing the coding style settings in Intellij

1. Download the [intellij-java-google-style.xml](https://raw.githubusercontent.com/google/styleguide/gh-pages/intellij-java-google-style.xml) file from the http://code.google.com/p/google-styleguide/ repo.
2. go into Preferences -> Editor -> Code Style. Click on Manage and import the downloaded Style Setting file. Select GoogleStyle as new coding style.

### Installing the coding style settings in Eclipse

1. Download the [eclipse-java-google-style.xml](https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml) file from the http://code.google.com/p/google-styleguide/ repo. 
2. Under Window/Preferences select Java/Code Style/Formatter. Import the settings file by selecting Import.

## Deployment

### CI pipeline workflow

How to run this project on CI(using Jenkins):

put following codes in the Jenkins Build config's Execute shell

```
# export CKB_DOCKER_IMAGE_TAG_NAME=v0.13.0 # set this ENV variable to specified test target version
# get available tag from https://hub.docker.com/r/nervos/ckb/tags

make setup # install requirements
make update # update ckb docker image
make test # run test cases
```
## Contributing

1. Know how to run tests: [docs/how-to-run-tests.md](docs/how-to-run-tests.md)
2. Know how to write tests: [docs/how-to-write-tests.md](docs/how-to-write-tests.md)
3. Know how to report issues: [new issue](https://github.com/nervosnetwork/ckb-system-testing/issues/new )

### Commit your changes

#### Workflow

[GitHub Flow](https://help.github.com/en/articles/github-flow), [Understanding the GitHub flow](https://guides.github.com/introduction/flow/)

#### git style guide

use [git-style-guide](https://github.com/agis/git-style-guide) for Branches, Commits,Messages, Merging

## Versioning

We use [SemVer](http://semver.org/) for versioning.

## License

This project is licensed under the Apache 2.0 License

## Acknowledgments

* You should know how to use Maven.
* You should know how to write test cases using TestNG.
