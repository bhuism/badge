# GIT Badge Service

[![Api Status](https://img.shields.io/badge/dynamic/json?color=4c1&label=api&query=%24.status&url=https%3A%2F%2Fbadge.odee.net%2Factuator%2Fhealth)](https://badge.odee.net/)
[![Version badge](https://img.shields.io/endpoint?url=https%3A%2F%2Fbadge.odee.net%2Fgithub%2Factuator%2Fbhuism%2Fbadge%2Fmaster%3Flabel%3Dlatest%26actuator_url%3Dhttps%3A%2F%2Fbadge.odee.net%2Factuator%2Finfo)](https://badge.odee.net)
[![Build Status](https://travis-ci.com/bhuism/badge.svg?branch=master)](https://travis-ci.com/bhuism/badge)
[![Open Issues](https://img.shields.io/github/issues/bhuism/badge.svg)](https://github.com/bhuism/badge/issues)
[![License](https://img.shields.io/github/license/bhuism/badge.svg?color=4c1)](https://github.com/bhuism/badge/blob/master/LICENSE)
[![Maintainability](https://api.codeclimate.com/v1/badges/5ae2a1bef066937ec493/maintainability)](https://codeclimate.com/github/bhuism/badge/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/5ae2a1bef066937ec493/test_coverage)](https://codeclimate.com/github/bhuism/badge/test_coverage)
[![Known Vulnerabilities](https://snyk.io/test/github/bhuism/badge/badge.svg)](https://snyk.io/test/github/bhuism/badge)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bhuism_badge&metric=alert_status)](https://sonarcloud.io/dashboard?id=bhuism_badge)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=bhuism_badge&metric=bugs)](https://sonarcloud.io/dashboard?id=bhuism_badge)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=bhuism_badge&metric=code_smells)](https://sonarcloud.io/dashboard?id=bhuism_badge)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=bhuism_badge&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=bhuism_badge)

[Shields endpoint api](https://shields.io/endpoint) for showing if your app is latest or not.

When the commit_sha is known (in your app) use for shieldsio endoint:

https://badge.odee.net/github/sha/{user}/{repo}/{branch}/{commit_sha}

when the commit_sha is not known (outside your app) the commit_sha can be retreived with the spring info actuator:

https://badge.odee.net/github/sha/{user}/{repo}/{branch}?actuator_url={actuator_url}

Shieldsio Example:

https://img.shields.io/endpoint?url=https%3A%2F%2Fbadge.odee.net%2Fgithub%2Factuator%2Fbhuism%2Fbadge%2Fmaster%3Flabel%3Dlatest%26actuator_url%3Dhttps%3A%2F%2Fbadge.odee.net%2Factuator%2Finfo

Endpoint URL Example for commit_sha:

https://badge.odee.net/github/sha/bhuism/citaten/master/683ae55b512bc1c303d7157f1e43e58197fcf925?label=latest

Endpoint URL Example for actuator:

https://badge.odee.net/github/actuator/bhuism/badge/master?label=latest&actuator_url=https://badge.odee.net/actuator/info
