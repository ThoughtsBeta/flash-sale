# Redis Application plugin for Grafana

![Application](https://raw.githubusercontent.com/RedisGrafana/grafana-redis-app/master/src/img/redis-app.png)

[![Grafana 8](https://img.shields.io/badge/Grafana-8-orange)](https://www.grafana.com)
[![Redis Data Source](https://img.shields.io/badge/dynamic/json?color=blue&label=Redis%20Data%20Source&query=%24.version&url=https%3A%2F%2Fgrafana.com%2Fapi%2Fplugins%2Fredis-datasource)](https://grafana.com/grafana/plugins/redis-datasource)
[![Redis Application plugin](https://img.shields.io/badge/dynamic/json?color=blue&label=Redis%20Application%20plugin&query=%24.version&url=https%3A%2F%2Fgrafana.com%2Fapi%2Fplugins%2Fredis-app)](https://grafana.com/grafana/plugins/redis-app)
![CI](https://github.com/RedisGrafana/grafana-redis-app/workflows/CI/badge.svg)
![Docker](https://github.com/RedisGrafana/grafana-redis-app/workflows/Docker/badge.svg)
[![codecov](https://codecov.io/gh/RedisGrafana/grafana-redis-app/branch/master/graph/badge.svg?token=15SIRGU8SX)](https://codecov.io/gh/RedisGrafana/grafana-redis-app)
[![Language grade: JavaScript](https://img.shields.io/lgtm/grade/javascript/g/RedisGrafana/grafana-redis-app.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/RedisGrafana/grafana-redis-app/context:javascript)

## Summary

- [**Introduction**](#introduction)
- [**Getting Started**](#getting-started)
- [**Documentation**](#documentation)
- [**Development**](#development)
- [**Feedback**](#feedback)
- [**Contributing**](#contributing)
- [**License**](#license)

## Introduction

The Redis Application is a plugin for Grafana that provides application pages, custom panels, and dashboards for [Redis Data Source](https://grafana.com/grafana/plugins/redis-datasource).

### Custom Panels

- [Command-line interface (CLI)](https://redisgrafana.github.io/redis-app/panels/redis-cli-panel/)
- [Command Latency (graph and table)](https://redisgrafana.github.io/redis-app/panels/redis-latency-panel/)
- [Keys consuming a lot of memory](https://redisgrafana.github.io/redis-app/panels/redis-keys-panel/)
- [RedisGears Script Editor](https://redisgrafana.github.io/redis-app/panels/redis-gears-panel/)

### Dashboards

- [Redis CLI](https://redisgrafana.github.io/redis-app/dashboards/cli/)
- [Redis Overview](https://redisgrafana.github.io/redis-app/dashboards/overview/)
- [RedisGears](https://redisgrafana.github.io/redis-app/dashboards/redis-gears/)

All dashboards are available from the application's icon in the left side menu.

![Redis-CLI-Dashboards](https://raw.githubusercontent.com/RedisGrafana/grafana-redis-app/master/src/img/redis-cli-dashboard.png)

### Requirements

- **Grafana 8.0+** is required for Redis Application 2.X.
- **Grafana 7.1+** is required for Redis Application 1.X.

## Getting Started

Use the `grafana-cli` tool to install from the command line:

```bash
grafana-cli plugins install redis-app
```

For Docker instructions and installation without Internet access, follow the [Quickstart](https://redisgrafana.github.io/quickstart/) page.

### Open Grafana and enable Redis Application plugin

Open Grafana in your browser, enable Redis Application plugin, and configure Redis Data Sources.

![Enable](https://raw.githubusercontent.com/RedisGrafana/grafana-redis-app/master/src/img/enable.png)

## Documentation

Take a look at the [Documentation](https://redisgrafana.github.io/redis-app/overview/) to learn more about the Redis Application plugin, Redis Data Source, provided dashboards, and custom panels.

## Development

[Developing Redis Application plugin](https://redisgrafana.github.io/development/redis-app/) page provides instructions on building the application.

Are you interested in the latest features and updates? Start nightly built [Docker image for Redis Application plugin](https://redisgrafana.github.io/development/images/).

## Feedback

We love to hear from users, developers, and the whole community interested in this plugin. These are various ways to get in touch with us:

- Ask a question, request a new feature, and file a bug with [GitHub issues](https://github.com/RedisGrafana/grafana-redis-app/issues/new/choose).
- Star the repository to show your support.

## Contributing

- Fork the repository.
- Find an issue to work on and submit a pull request.
- Could not find an issue? Look for documentation, bugs, typos, and missing features.

## License

- Apache License Version 2.0, see [LICENSE](https://github.com/RedisGrafana/grafana-redis-app/blob/master/LICENSE).
