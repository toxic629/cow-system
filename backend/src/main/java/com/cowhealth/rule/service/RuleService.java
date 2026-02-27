package com.cowhealth.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cowhealth.common.BusinessException;
import com.cowhealth.common.PageResult;
import com.cowhealth.rule.entity.RuleConfig;
import com.cowhealth.rule.mapper.RuleConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RuleService {

    private final RuleConfigMapper ruleConfigMapper;

    public RuleService(RuleConfigMapper ruleConfigMapper) {
        this.ruleConfigMapper = ruleConfigMapper;
    }

    public PageResult<RuleConfig> page(long page, long size) {
        Page<RuleConfig> p = ruleConfigMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<RuleConfig>().orderByDesc(RuleConfig::getUpdatedAt));
        return new PageResult<>(page, size, p.getTotal(), p.getRecords());
    }

    public List<RuleConfig> listEnabled() {
        return ruleConfigMapper.selectList(new LambdaQueryWrapper<RuleConfig>().eq(RuleConfig::getEnabled, true));
    }

    @Transactional
    public void create(RuleConfig rule) {
        rule.setRuleId(null);
        LocalDateTime now = LocalDateTime.now();
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        if (rule.getEnabled() == null) {
            rule.setEnabled(Boolean.TRUE);
        }
        ruleConfigMapper.insert(rule);
    }

    @Transactional
    public void update(RuleConfig rule) {
        if (rule.getRuleId() == null || ruleConfigMapper.selectById(rule.getRuleId()) == null) {
            throw new BusinessException(404, "Rule not found");
        }
        rule.setUpdatedAt(LocalDateTime.now());
        ruleConfigMapper.updateById(rule);
    }

    @Transactional
    public void delete(Long id) {
        if (ruleConfigMapper.deleteById(id) == 0) {
            throw new BusinessException(404, "Rule not found");
        }
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        RuleConfig rule = ruleConfigMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(404, "Rule not found");
        }
        rule.setEnabled(enabled);
        rule.setUpdatedAt(LocalDateTime.now());
        ruleConfigMapper.updateById(rule);
    }
}
