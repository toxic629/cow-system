DROP TABLE IF EXISTS alarm_action_log;
DROP TABLE IF EXISTS alarm;
DROP TABLE IF EXISTS baseline_hourly;
DROP TABLE IF EXISTS rule_config;
DROP TABLE IF EXISTS observation_raw;
DROP TABLE IF EXISTS observation_minute_agg;
DROP TABLE IF EXISTS cow;

CREATE TABLE cow (
    cow_id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    ear_tag VARCHAR(64) NOT NULL UNIQUE,
    barn_id VARCHAR(32) NOT NULL,
    pen VARCHAR(32),
    age_days INT NOT NULL,
    parity INT DEFAULT 0,
    lactation_day INT DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cow_barn_status (barn_id, status)
);

CREATE TABLE observation_raw (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cow_id VARCHAR(32) NOT NULL,
    barn_id VARCHAR(32) NOT NULL,
    metric_name VARCHAR(64) NOT NULL,
    ts DATETIME NOT NULL,
    value DOUBLE NOT NULL,
    quality_flag VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ob_raw (cow_id, barn_id, metric_name, ts),
    INDEX idx_ob_raw_metric_ts (metric_name, ts)
);

CREATE TABLE observation_minute_agg (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cow_id VARCHAR(32) NOT NULL,
    barn_id VARCHAR(32) NOT NULL,
    metric_name VARCHAR(64) NOT NULL,
    ts_minute DATETIME NOT NULL,
    sum_value DOUBLE NOT NULL DEFAULT 0,
    min_value DOUBLE NULL,
    max_value DOUBLE NULL,
    count_value BIGINT NOT NULL DEFAULT 0,
    bad_count BIGINT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ob_minute (cow_id, barn_id, metric_name, ts_minute),
    INDEX idx_ob_minute_query (cow_id, barn_id, metric_name, ts_minute)
);

CREATE TABLE rule_config (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    severity VARCHAR(8) NOT NULL,
    type VARCHAR(32) NOT NULL,
    metric_name VARCHAR(64) NOT NULL,
    upper DOUBLE NULL,
    lower DOUBLE NULL,
    duration_minutes INT DEFAULT 1,
    silence_minutes INT DEFAULT 30,
    composite_json TEXT NULL,
    barn_scope VARCHAR(32) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE alarm (
    alarm_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cow_id VARCHAR(32) NOT NULL,
    barn_id VARCHAR(32) NOT NULL,
    rule_id BIGINT NOT NULL,
    severity VARCHAR(8) NOT NULL,
    status VARCHAR(32) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NULL,
    metric_name VARCHAR(64) NOT NULL,
    trigger_value DOUBLE NULL,
    threshold_json TEXT NULL,
    deviation_score DOUBLE NULL,
    evidence_json TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_alarm_query (severity, status, cow_id, barn_id, metric_name, start_time),
    INDEX idx_alarm_rule_cow_status (rule_id, cow_id, status)
);

CREATE TABLE alarm_action_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alarm_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    note VARCHAR(255) NULL,
    operator VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_alarm_action_alarm (alarm_id)
);

CREATE TABLE baseline_hourly (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cow_id VARCHAR(32) NOT NULL,
    metric_name VARCHAR(64) NOT NULL,
    hour_of_day INT NOT NULL,
    mean DOUBLE NOT NULL,
    std DOUBLE NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_baseline (cow_id, metric_name, hour_of_day)
);
