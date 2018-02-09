# utPLSQL for SQL Developer

## Introduction

utPLSQL for SQL Developer extends Oracle's SQL Developer by context menu entries and keyboard shortcuts for running utPLSQL unit tests from the Connections window or the PL/SQL editor.

[![utPLSQL for SQL Developer Demo](utPLSQL.png)](utPLSQL.gif)

## Releases

Binary releases are published [here](https://github.com/utPLSQL/utPLSQL-SQLDeveloper/releases).

## Installation

### From file

1. Start SQL Developer

2. Select ```Check for Updates…``` in the help menu.

3. Use the ```Install From Local File``` option to install the previously downloaded ```utplsql_for_SQLDev_*.zip``` file.

## Issues
Please file your bug reports, enhancement requests, questions and other support requests within [Github's issue tracker](https://help.github.com/articles/about-issues/).

* [Questions](https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues?q=is%3Aissue+label%3Aquestion)
* [Open enhancements](https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement)
* [Open bugs](https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues?q=is%3Aopen+is%3Aissue+label%3Abug)
* [Submit new issue](https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/new)

## How to Contribute

1. Describe your idea by [submitting an issue](https://github.com/utPLSQL/utPLSQL-SQLDeveloper/issues/new)
2. [Fork the utPLSQL-SQLDeveloper respository](https://github.com/utPLSQL/utPLSQL-SQLDeveloper/fork)
3. [Create a branch](https://help.github.com/articles/creating-and-deleting-branches-within-your-repository/), commit and publish your changes and enhancements
4. [Create a pull request](https://help.github.com/articles/creating-a-pull-request/)

## How to Build

1. [Download](http://www.oracle.com/technetwork/developer-tools/sql-developer/downloads/index.html) and install SQL Developer 17.4.0
2. [Download](https://maven.apache.org/download.cgi) and install Apache Maven 3.5.2
3. [Download](https://git-scm.com/downloads) and install a git command line client
4. Clone the utPLSQL-SQLDeveloper repository
5. Open a terminal window in the utPLSQL-SQLDeveloper root folder and type

		cd sqldev

6. Run maven build by the following command

		mvn -Dsqldev.basedir=/Applications/SQLDeveloper17.4.0.app/Contents/Resources/sqldeveloper -DskipTests=true clean package

	Amend the parameter sqldev.basedir to match the path of your SQL Developer installation. This folder is used to reference Oracle jar files which are not available in public Maven repositories
7. The resulting file ```utplsql_for_SQLDev_x.x.x-SNAPSHOT.zip``` in the ```target``` directory may be installed within SQL Developer

## License

utPLSQL for SQL Developer is licensed under the Apache License, Version 2.0. You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
