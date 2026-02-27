package com.cowhealth.observation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cowhealth.common.BusinessException;
import com.cowhealth.observation.dto.AggPoint;
import com.cowhealth.observation.dto.CsvImportResult;
import com.cowhealth.observation.dto.IngestResult;
import com.cowhealth.observation.dto.ObservationItem;
import com.cowhealth.observation.entity.ObservationMinuteAgg;
import com.cowhealth.observation.entity.ObservationRaw;
import com.cowhealth.observation.enums.QualityFlag;
import com.cowhealth.observation.mapper.ObservationMinuteAggMapper;
import com.cowhealth.observation.mapper.ObservationRawMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ObservationService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObservationRawMapper rawMapper;
    private final ObservationMinuteAggMapper aggMapper;
    private final QualityService qualityService;

    public ObservationService(ObservationRawMapper rawMapper, ObservationMinuteAggMapper aggMapper, QualityService qualityService) {
        this.rawMapper = rawMapper;
        this.aggMapper = aggMapper;
        this.qualityService = qualityService;
    }

    @Transactional
    public IngestResult ingestBatch(List<ObservationItem> items) {
        int success = 0;
        int duplicate = 0;
        int bad = 0;
        for (ObservationItem item : items) {
            QualityFlag quality = qualityService.evaluate(item.getCowId(), item.getMetricName(), item.getValue());
            if (quality == QualityFlag.BAD) {
                bad++;
            }
            ObservationRaw raw = new ObservationRaw();
            raw.setCowId(item.getCowId());
            raw.setBarnId(item.getBarnId());
            raw.setMetricName(item.getMetricName());
            raw.setTs(item.getTimestamp());
            raw.setValue(item.getValue());
            raw.setQualityFlag(quality.name());
            raw.setCreatedAt(LocalDateTime.now());
            try {
                rawMapper.insert(raw);
                upsertMinuteAgg(item, quality);
                success++;
            } catch (DuplicateKeyException e) {
                duplicate++;
            }
        }
        return new IngestResult(success, duplicate, bad);
    }

    @Transactional
    public CsvImportResult importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "CSV file is empty");
        }
        List<ObservationItem> items = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try (CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            for (CSVRecord rec : parser) {
                try {
                    ObservationItem item = new ObservationItem();
                    item.setCowId(rec.get("cow_id"));
                    item.setBarnId(rec.get("barn_id"));
                    item.setMetricName(rec.get("metric_name"));
                    item.setTimestamp(LocalDateTime.parse(rec.get("timestamp"), DATETIME_FMT));
                    item.setValue(Double.valueOf(rec.get("value")));
                    items.add(item);
                } catch (Exception ex) {
                    errors.add("line " + rec.getRecordNumber() + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new BusinessException(400, "Unable to parse CSV: " + e.getMessage());
        }
        IngestResult result = ingestBatch(items);
        return new CsvImportResult(result.getSuccessCount(), errors);
    }

    public List<AggPoint> query(String cowId, String barnId, String metric,
                                LocalDateTime from, LocalDateTime to, String granularity) {
        if (!StringUtils.hasText(cowId) || !StringUtils.hasText(barnId) || !StringUtils.hasText(metric)) {
            throw new BusinessException(400, "cowId, barnId and metric are required");
        }
        if (from == null || to == null) {
            throw new BusinessException(400, "from and to are required");
        }
        if ("minute".equalsIgnoreCase(granularity)) {
            return aggMapper.queryMinute(cowId, barnId, metric, from, to);
        }
        if ("hour".equalsIgnoreCase(granularity) || "day".equalsIgnoreCase(granularity)) {
            return aggMapper.queryRollup(cowId, barnId, metric, from, to, granularity.toLowerCase());
        }
        throw new BusinessException(400, "granularity must be minute|hour|day");
    }

    private void upsertMinuteAgg(ObservationItem item, QualityFlag quality) {
        LocalDateTime tsMinute = item.getTimestamp().truncatedTo(ChronoUnit.MINUTES);
        LambdaQueryWrapper<ObservationMinuteAgg> query = new LambdaQueryWrapper<>();
        query.eq(ObservationMinuteAgg::getCowId, item.getCowId())
                .eq(ObservationMinuteAgg::getBarnId, item.getBarnId())
                .eq(ObservationMinuteAgg::getMetricName, item.getMetricName())
                .eq(ObservationMinuteAgg::getTsMinute, tsMinute);
        ObservationMinuteAgg current = aggMapper.selectOne(query);
        if (current == null) {
            ObservationMinuteAgg agg = new ObservationMinuteAgg();
            agg.setCowId(item.getCowId());
            agg.setBarnId(item.getBarnId());
            agg.setMetricName(item.getMetricName());
            agg.setTsMinute(tsMinute);
            if (quality == QualityFlag.BAD) {
                agg.setSumValue(0.0);
                agg.setMinValue(null);
                agg.setMaxValue(null);
                agg.setCountValue(0L);
                agg.setBadCount(1L);
            } else {
                agg.setSumValue(item.getValue());
                agg.setMinValue(item.getValue());
                agg.setMaxValue(item.getValue());
                agg.setCountValue(1L);
                agg.setBadCount(0L);
            }
            agg.setUpdatedAt(LocalDateTime.now());
            aggMapper.insert(agg);
            return;
        }
        if (quality == QualityFlag.BAD) {
            current.setBadCount(current.getBadCount() + 1);
        } else {
            current.setSumValue(current.getSumValue() + item.getValue());
            current.setCountValue(current.getCountValue() + 1);
            if (current.getMinValue() == null || item.getValue() < current.getMinValue()) {
                current.setMinValue(item.getValue());
            }
            if (current.getMaxValue() == null || item.getValue() > current.getMaxValue()) {
                current.setMaxValue(item.getValue());
            }
        }
        current.setUpdatedAt(LocalDateTime.now());
        aggMapper.updateById(current);
    }
}
