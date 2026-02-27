package com.cowhealth.cow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cowhealth.cow.entity.Cow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CowMapper extends BaseMapper<Cow> {
}
