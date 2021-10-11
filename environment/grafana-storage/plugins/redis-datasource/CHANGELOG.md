# Change Log

## 1.4.0 (2021-05-08)

### Features / Enhancements

- Update Grafana SDK 0.88 and other backend dependencies (#170)
- Add $time field for Streams XRANGE (#175)
- Add RG.PYDUMPREQS command and integration test fix (#183)
- Add Integration tests to CI (#184)
- Upgrade Grafana dependencies to 7.5.4 (#185)
- Update Dashboard to 7.5.4 and add data source variable (#186)
- Update backend dependencies and linting issues (#187)
- Update Documentation (#188)

### Bug fixes

- Tls client certificates not working (#177)

## 1.3.1 (2021-02-04)

### Features / Enhancements

- Add Unit test for Golang Backend #119
- Remove "Unknown command" error from response for custom panels #125
- Update Radix to 3.7.0 and other backend dependencies #128
- Redis client, unit-tests refactoring and new unit-tests. #129
- Implement CLI-mode similar to Redis-cli #135
- Added support for errorstats features coming in redis 6.2; Extended commandstats fields with failedCalls and rejectedCalls #137
- Add command to support the panel to show the biggest keys (TMSCAN) #133
- Add RedisGears commands (RG.PYSTATS, RG.DUMPREGISTRATIONS, RG.PYEXECUTE) #136
- Implement XRANGE and XREVRANGE commands #148
- Add Client Type tooltip #149
- Refactoring Query Editor #151
- Add handling different frame type for Streaming data source #152
- Update tooltip for RedisTimeSeries Label Filter #155
- Update Loading state for Streaming for Grafana 7.4 #158
- Update Grafana SDK 0.86 to fix race conditions #160
- Add Redis Graph module (GRAPH.QUERY, GRAPH.SLOWLOG) #157

### Bug fixes

- Experiencing memory leak in Grafana docker seemingly stemming from this plugin #116
- All Redis Datasource timeout when one is not reachable #73

## 1.3.0 (2021-01-05)

### Breaking changes

- HGETALL returns hash fields in a row similar to HGET, HMGET to support streaming. Previously each hash field returned as row.
- Time Bucket for RedisTimeSeries TS.RANGE and TS.MRANGE was updated from string to integer. To fix the dashboard JSON:
  - Search for `"bucket"="X"`
  - Remove quotes
- RedisTimeSeries TS.RANGE command was updated to have legend and value override similar to TS.MRANGE. Previous `legend` defined field's name.
- `key` parameter for command like GET, HGET, SMEMBERS was updated to `keyName` to avoid conflicts. To fix the dashboard JSON:
  - Search for `"key"="X"`
  - Replace to `"keyName"="X"`

### Features / Enhancements

- Update description and GitHub issues #83
- Add RediSearch FT.INFO command #97
- Add HMGET Command #98
- Update release workflow #99
- Update Grafana dependencies to 7.3.5 #100
- Update Grafana SDK 0.80.0 #101
- Update data source icon and refactoring #102
- Update field's name for HGET command to align with HMGET #103
- Update HGETALL command to return fields and support streaming similar to HGET, HMGET #104
- Add tests for React Config and Query editors #105
- Remove CircleCI and move to Github Actions #106
- Update Bucket's type (string->number) and add type values for Aggregation and Info sections #108
- Add tests for React Data Source #113
- Update Bucket to Time Bucket in Query Editor #114
- Check if string value is a number when streaming #115
- Add Tests Coverage #117
- Add Empty Array when no values returned similar to redis-cli #120, #121
- Add test data for backend testing #122

### Bug fixes

- Fix "NOAUTH Authentication required" error with sentinel #109
- Add Value Label to TS.RANGE command similar to TS.MRANGE #110
- Update default configuration parameters for Data Source #111
- Update Key to KeyName to avoid conflict in the Explore tab #112

## 1.2.1 (2020-10-24)

### Features / Enhancements

- Support Connecting to Redis via Unix Socket #58
- Support Redis 6 ACL authentication #60
- Update Grafana dependencies to 7.2.0 #66
- Update and optimize dashboards for Grafana 7.2.0 #67
- Add Streaming for Command Statistics #68
- Add Size parameter for SLOWLOG GET #79
- Update GitHub org to RedisGrafana #80

### Bug fixes

- Plugin health check failed for ARM on Linux #61
- Timeseries data time stamp truncated to seconds #64

## 1.2.0 (2020-08-26)

### Features / Enhancements

- Added docker cmd line option to start in README #31
- How to query a specific database inside the same Redis single node #34
- Update docker-compose to load datasource from the repository and add development file #39
- Use "ScopedVars" when applying template variables #37 (fix for #36)
- Refactoring to support new commands and modules #42
- Add support for TS.GET, TS.INFO, and TS.QUERYINDEX commands #45
- Add Redis dashboard to support multiple Redis instances #49
- Plugin executable missing for arm64 architecture #48 (Grafana SDK: https://github.com/grafana/grafana-plugin-sdk-go/pull/221)
- Return 0 for all buckets with 0 counts on time-series `TS.RANGE` queries #50
- Add Redis Cluster support and update monitoring dashboard #52
- Connection issue to Redis deployed in k8s (Sentinel) #38
- MRANGE: add fill zero option #53
- Add Streaming capabilities to visualize INFO command #57

### Bug fixes

- Slowlog returns 'No data' for Redis 3.0.6 #33
- Fix backend lint issues #41
- ts.mrange returns no data when label has spaces within #44

## 1.1.2 (2020-07-29)

### Features / Enhancements

- Remove developer jargon from README #30
- Redis Datasource is Unsigned. K8S+Helm installation #29

## 1.1.1 (2020-07-28)

### Features / Enhancements

- Screenshots added to plugin.json and updated in the README
- CHANGELOG added to display on the Plugin page

## 1.1.0 (2020-07-24)

### Features / Enhancements

- Updated to Grafana 7.1.0 and the latest version of Radix #27
- Add dashboard as a part of datasource #25
- Add Field config units to the response #26

## 1.0.0 (2020-07-13)

### Features / Enhancements

- Initial release based on Grafana 7.0.5.
- Allows configuring password, TLS, and advanced settings.
- Supports Redis commands: CLIENT LIST, GET, HGET, HGETALL, HKEYS, HLEN, INFO, LLEN, SCARD, SLOWLOG GET, SMEMBERS, TTL, TYPE, XLEN.
- Supports RedisTimeSeries commands: TS.MRANGE, TS.RANGE.
- Provides Redis monitoring dashboard.
