package com.cowhealth.alarm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cowhealth.alarm.entity.Alarm;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlarmMapper extends BaseMapper<Alarm> {
}
