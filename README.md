# IBM Bob hackathon: JPetStore

**Active submission application:** [JPetStore](jpetstore/README.md), imported from
[mybatis/jpetstore-6](https://github.com/mybatis/jpetstore-6) at commit
`1ffdf1e91b62aa32abfc4511f548c9bdc84b9bbf`. The imported code remains
upstream code under its own [LICENSE](jpetstore/LICENSE) and
[NOTICE](jpetstore/NOTICE). The import and these planning documents are Codex
preparation, not IBM Bob work. No Bob-assisted fix or feature is claimed yet.

Open this repository in Bob, then work in `jpetstore/`. See
[HACKATHON.md](HACKATHON.md), [SPEC.md](SPEC.md), and
[VERIFICATION.md](VERIFICATION.md) for scope and evidence.

## Prior IBM i exploration

The existing IBM i COBOL examples and their Git history remain below. They
were an earlier hackathon direction and are not the active submission app.

This repository began as a
[fork of Jack Woehr's examples](https://github.com/jwoehr/ibmi-cobol-examples).
The original examples and their history remain under the root Apache 2.0
license. The following sections describe that prior IBM i work.

Examples of programming COBOL on the [IBM i](https://www.ibm.com/products/ibm-i) platform using [IBM Bob](https://bob.ibm.com/) agentic code assist - **These are examples, and are not production code**. See LICENSE for limit of liability.

## Purpose of the project

This project demonstrates usage of agentic code assist to code modern COBOL projects on IBM i. The author's intent is to continue to add examples based on the original content.

It is focused on the mechanics of writing modern ILE COBOL rather than useful tools. These are examples of technique rather than of content.

## Contents of the project

### `picclaus`

This ILE COBOL program + display file project provides a TN5250 "green screen" application which allows the user to scroll through a list of allowable COBOL `PICTURE` clauses.

### `cobolinfo`

This ILE COBOL program + service program + display file project extends the `picclaus` application a TN5250 "green screen" application in 2 ways:

1. It extracts the content displayed by the program into a service program called by the main application program.
1. It adds a second mode which allows the user to scroll through a list of allowable COBOL verbs.

## Usage

- Deploy this project as a workspace to the IFS of your IBM i.
  - `IBM i: Deploy workspace` in IBM Bob.
- Execute the desired `Makefile` to build and install the application in the library of your choice.

See the individual `Makefile`s for more information on the particular programs.

## Future direction

The author plans to wrap the service program entries as web APIs.

- _Jack Woehr, Beulah, Colorado, 2026-09-20_
