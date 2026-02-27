INSERT INTO cow (cow_id, name, ear_tag, barn_id, pen, age_days, parity, lactation_day, status)
VALUES
('COW001', 'Cow-001', 'ET00001', 'B1', 'P1', 651, 1, 21, 'NORMAL'),
('COW002', 'Cow-002', 'ET00002', 'B1', 'P2', 652, 2, 22, 'NORMAL'),
('COW003', 'Cow-003', 'ET00003', 'B1', 'P3', 653, 3, 23, 'NORMAL'),
('COW004', 'Cow-004', 'ET00004', 'B1', 'P4', 654, 0, 24, 'NORMAL'),
('COW005', 'Cow-005', 'ET00005', 'B1', 'P5', 655, 1, 25, 'NORMAL'),
('COW006', 'Cow-006', 'ET00006', 'B1', 'P6', 656, 2, 26, 'NORMAL'),
('COW007', 'Cow-007', 'ET00007', 'B1', 'P7', 657, 3, 27, 'OBSERVE'),
('COW008', 'Cow-008', 'ET00008', 'B1', 'P8', 658, 0, 28, 'NORMAL'),
('COW009', 'Cow-009', 'ET00009', 'B1', 'P9', 659, 1, 29, 'NORMAL'),
('COW010', 'Cow-010', 'ET00010', 'B1', 'P10', 660, 2, 30, 'TREATMENT'),
('COW011', 'Cow-011', 'ET00011', 'B1', 'P1', 661, 3, 31, 'NORMAL'),
('COW012', 'Cow-012', 'ET00012', 'B1', 'P2', 662, 0, 32, 'NORMAL'),
('COW013', 'Cow-013', 'ET00013', 'B1', 'P3', 663, 1, 33, 'NORMAL'),
('COW014', 'Cow-014', 'ET00014', 'B1', 'P4', 664, 2, 34, 'OBSERVE'),
('COW015', 'Cow-015', 'ET00015', 'B1', 'P5', 665, 3, 35, 'NORMAL'),
('COW016', 'Cow-016', 'ET00016', 'B2', 'P6', 666, 0, 36, 'NORMAL'),
('COW017', 'Cow-017', 'ET00017', 'B2', 'P7', 667, 1, 37, 'NORMAL'),
('COW018', 'Cow-018', 'ET00018', 'B2', 'P8', 668, 2, 38, 'NORMAL'),
('COW019', 'Cow-019', 'ET00019', 'B2', 'P9', 669, 3, 39, 'NORMAL'),
('COW020', 'Cow-020', 'ET00020', 'B2', 'P10', 670, 0, 40, 'TREATMENT'),
('COW021', 'Cow-021', 'ET00021', 'B2', 'P1', 671, 1, 41, 'OBSERVE'),
('COW022', 'Cow-022', 'ET00022', 'B2', 'P2', 672, 2, 42, 'NORMAL'),
('COW023', 'Cow-023', 'ET00023', 'B2', 'P3', 673, 3, 43, 'NORMAL'),
('COW024', 'Cow-024', 'ET00024', 'B2', 'P4', 674, 0, 44, 'NORMAL'),
('COW025', 'Cow-025', 'ET00025', 'B2', 'P5', 675, 1, 45, 'NORMAL'),
('COW026', 'Cow-026', 'ET00026', 'B2', 'P6', 676, 2, 46, 'NORMAL'),
('COW027', 'Cow-027', 'ET00027', 'B2', 'P7', 677, 3, 47, 'NORMAL'),
('COW028', 'Cow-028', 'ET00028', 'B2', 'P8', 678, 0, 48, 'OBSERVE'),
('COW029', 'Cow-029', 'ET00029', 'B2', 'P9', 679, 1, 49, 'NORMAL'),
('COW030', 'Cow-030', 'ET00030', 'B2', 'P10', 680, 2, 50, 'TREATMENT');

INSERT INTO rule_config
(name, enabled, severity, type, metric_name, upper, lower, duration_minutes, silence_minutes, composite_json, barn_scope)
VALUES
('Respiratory Threshold', 1, 'P1', 'THRESHOLD', 'resp_rate', 70, NULL, 1, 30, NULL, NULL),
('Respiratory Duration', 1, 'P2', 'DURATION', 'resp_rate', 65, NULL, 3, 30, NULL, NULL),
('Heat Stress Composite', 1, 'P1', 'COMPOSITE', 'resp_rate', 70, NULL, 1, 20,
 '{"metric":"resp_rate","upper":70,"tempMetric":"temp_c","tempUpper":39,"humidityMetric":"humidity","humidityUpper":80}', NULL),
('Resp Offline', 1, 'P3', 'OFFLINE', 'resp_rate', NULL, NULL, 120, 60, NULL, NULL);
