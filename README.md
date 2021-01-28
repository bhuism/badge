# Deploy status Badge Service

[![Version badge](https://badge.odee.net/github/actuator/bhuism/badge/master/badge.svg?actuator_url=https://badge.odee.net/actuator/info)](https://badge.odee.net)
[![versionspringboot](https://img.shields.io/badge/dynamic/xml?color=brightgreen&url=https://raw.githubusercontent.com/bhuism/badge/master/pom.xml&query=%2F%2A%5Blocal-name%28%29%3D%27project%27%5D%2F%2A%5Blocal-name%28%29%3D%27parent%27%5D%2F%2A%5Blocal-name%28%29%3D%27version%27%5D&label=springboot)](https://github.com/spring-projects/spring-boot)
[![versionspring-graalvm-native](https://img.shields.io/badge/dynamic/xml?color=brightgreen&url=https://raw.githubusercontent.com/bhuism/badge/master/pom.xml&query=%2F%2F%2A%5Blocal-name%28%29%3D%27graalvm.version%27%5D&label=graalvm)](https://github.com/spring-projects-experimental/spring-graalvm-native)
[![Api Status](https://img.shields.io/badge/dynamic/json?color=4c1&label=api&query=%24.status&url=https%3A%2F%2Fbadge.odee.net%2Factuator%2Fhealth)](https://badge.odee.net/)
[![Open Issues](https://img.shields.io/github/issues/bhuism/badge.svg)](https://github.com/bhuism/badge/issues)
[![License](https://img.shields.io/github/license/bhuism/badge.svg?color=4c1)](https://github.com/bhuism/badge/blob/master/LICENSE)
[![Maintainability](https://api.codeclimate.com/v1/badges/5ae2a1bef066937ec493/maintainability)](https://codeclimate.com/github/bhuism/badge/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/5ae2a1bef066937ec493/test_coverage)](https://codeclimate.com/github/bhuism/badge/test_coverage)
[![Known Vulnerabilities](https://snyk.io/test/github/bhuism/badge/badge.svg)](https://snyk.io/test/github/bhuism/badge)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bhuism_badge&metric=alert_status)](https://sonarcloud.io/dashboard?id=bhuism_badge)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=bhuism_badge&metric=bugs)](https://sonarcloud.io/dashboard?id=bhuism_badge)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=bhuism_badge&metric=code_smells)](https://sonarcloud.io/dashboard?id=bhuism_badge)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=bhuism_badge&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=bhuism_badge)

Description
-----------

https://badge.odee.net/ is a flexible badge image generator showing if your deployed app is latest or not.

Docker image availlable at https://hub.docker.com/r/bhuism/badge, just:

```
docker run -p 8080:8080 bhuism/badge
```

Build & Run
-----------

```
./run.sh
```

Usage
-----

When the current deployed git commit_sha is known, you can generate this link in your app:

```
<img src="https://badge.odee.net/github/sha/{user}/{repo}/{branch}/{commit_sha}/badge.svg"></img>
```

Example:

https://badge.odee.net/github/sha/bhuism/badge/master/29d4e9731a09f535a230570a5be96c5c91e7a7ec/badge.svg

When the commit_sha is not known (outside your app) the commit_sha can be retreived with the spring info actuator:

```
<img src="https://badge.odee.net/github/actuator/{user}/{repo}/{branch}/badge.svg?actuator_url={actuator_url}"></img>
```

(Don't forget to urlencode the actualtor_url request parameter)

Example:

https://badge.odee.net/github/actuator/bhuism/badge/master/badge.svg?actuator_url=https%3A%2F%2Fbadge.odee.net%2Factuator%2Finfo

GitLab Example:

```
<img src="https://badge.odee.net/gitlab/sha/{id}/{branch}/{commit_sha}/badge.svg"></img>
```

```
https://badge.odee.net/gitlab/sha/12289803/master/14021d07/badge.svg
```

ShieldsIo endpoint Example:

[Shields endpoint api](https://shields.io/endpoint) for showing if your app is latest or not.

```
https://img.shields.io/endpoint?url=https%3A%2F%2Fbadge.odee.net%2Fgithub%2Factuator%2Fbhuism%2Fbadge%2Fmaster%3Factuator_url%3Dhttps%3A%2F%2Fbadge.odee.net%2Factuator%2Finfo
```

You can also have ShieldsIo generate the image with this endpoint for https://shields.io/endpoint :

```
https://badge.odee.net/github/sha/{user}/{repo}/{branch}/{commit_sha}
```

Example:

```
https://badge.odee.net/github/sha/bhuism/badge/master/29d4e9731a09f535a230570a5be96c5c91e7a7ec
```

ShieldsIo Endpoint URL Example for actuator:

```
https://badge.odee.net/github/sha/{user}/{repo}/{branch}?actuator_url={actuator_url}
```

Example:

```
https://img.shields.io/endpoint?url=https%3A%2F%2Fbadge.odee.net%2Fgithub%2Factuator%2Fbhuism%2Fbadge%2Fmaster%3Factuator_url%3Dhttps%3A%2F%2Fbadge.odee.net%2Factuator%2Finfo
```
