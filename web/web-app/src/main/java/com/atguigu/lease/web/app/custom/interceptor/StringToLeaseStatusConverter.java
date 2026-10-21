package com.atguigu.lease.web.app.custom.interceptor;

import com.atguigu.lease.model.enums.LeaseStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToLeaseStatusConverter implements Converter<String, LeaseStatus> {
    @Override
    public LeaseStatus convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            int code = Integer.parseInt(source);
            return LeaseStatus.fromCode(code);
        } catch (NumberFormatException e) {
            // 如果不是纯数字，可能传的是枚举名字（如 "RENTED"），这里做降级处理
            return LeaseStatus.valueOf(source.toUpperCase());
        }
    }
}