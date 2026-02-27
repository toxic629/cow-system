package com.cowhealth.cow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cowhealth.common.BusinessException;
import com.cowhealth.common.PageResult;
import com.cowhealth.cow.dto.CowUpsertRequest;
import com.cowhealth.cow.entity.Cow;
import com.cowhealth.cow.mapper.CowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class CowService {

    private final CowMapper cowMapper;

    public CowService(CowMapper cowMapper) {
        this.cowMapper = cowMapper;
    }

    public PageResult<Cow> page(long page, long size, String barnId, String status, String keyword) {
        LambdaQueryWrapper<Cow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(barnId), Cow::getBarnId, barnId)
                .eq(StringUtils.hasText(status), Cow::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w.like(Cow::getCowId, keyword)
                        .or().like(Cow::getName, keyword)
                        .or().like(Cow::getEarTag, keyword))
                .orderByDesc(Cow::getUpdatedAt);
        Page<Cow> p = cowMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(page, size, p.getTotal(), p.getRecords());
    }

    @Transactional
    public void create(CowUpsertRequest request) {
        if (cowMapper.selectById(request.getCowId()) != null) {
            throw new BusinessException(400, "cow_id already exists");
        }
        Cow cow = toEntity(request);
        LocalDateTime now = LocalDateTime.now();
        cow.setCreatedAt(now);
        cow.setUpdatedAt(now);
        cowMapper.insert(cow);
    }

    @Transactional
    public void update(CowUpsertRequest request) {
        Cow exists = cowMapper.selectById(request.getCowId());
        if (exists == null) {
            throw new BusinessException(404, "Cow not found");
        }
        Cow cow = toEntity(request);
        cow.setCreatedAt(exists.getCreatedAt());
        cow.setUpdatedAt(LocalDateTime.now());
        cowMapper.updateById(cow);
    }

    @Transactional
    public void delete(String cowId) {
        if (cowMapper.deleteById(cowId) == 0) {
            throw new BusinessException(404, "Cow not found");
        }
    }

    private Cow toEntity(CowUpsertRequest req) {
        Cow cow = new Cow();
        cow.setCowId(req.getCowId());
        cow.setName(req.getName());
        cow.setEarTag(req.getEarTag());
        cow.setBarnId(req.getBarnId());
        cow.setPen(req.getPen());
        cow.setAgeDays(req.getAgeDays());
        cow.setParity(req.getParity());
        cow.setLactationDay(req.getLactationDay());
        cow.setStatus(req.getStatus());
        return cow;
    }
}
