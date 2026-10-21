package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.common.utils.VerifyCodeUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

@Autowired
private StringRedisTemplate stringRedisTemplate;

@Autowired
private UserInfoMapper userInfoMapper;
    @Override
    public void getCode(String phone) {
        if (!StringUtils.hasText(phone)){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        String key= RedisConstant.ADMIN_LOGIN_PREFIX+phone;
        boolean hasKey  = stringRedisTemplate.hasKey(key);
        if(!hasKey){
            //若存在，则检查其存在的时间
            Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (RedisConstant.APP_LOGIN_CODE_TTL_SEC - expire < RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC) {
                //若存在时间不足一分钟，响应发送过于频繁
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
            String verifyCode = VerifyCodeUtil.getVerifyCode(6);
            stringRedisTemplate.opsForValue().set(key, verifyCode, RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);
            System.out.println("code:"+verifyCode);
        }

    }

    @Override
    public String login(LoginVo loginVo) {
        if (loginVo.getPhone()==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        if (loginVo.getCode()==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }
        String key = RedisConstant.ADMIN_LOGIN_PREFIX+loginVo.getPhone();
        String code=stringRedisTemplate.opsForValue().get(key);
        if (code==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }
        if(!(code.equals(loginVo.getCode()))){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPhone,loginVo.getPhone());
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        if (userInfo==null){
            userInfo = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("用户-"+loginVo.getPhone().substring(7));
            userInfoMapper.insert(userInfo);
        }else {
            if (userInfo.getStatus()==BaseStatus.DISABLE)
            {
                throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
            }
        }
        return JwtUtil.createToken(userInfo.getId(),userInfo.getNickname());
    }

    @Override
    public UserInfoVo getLoginUserById(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        UserInfoVo userInfoVo = new UserInfoVo(userInfo.getNickname(), userInfo.getAvatarUrl());
        return userInfoVo;
    }
}
