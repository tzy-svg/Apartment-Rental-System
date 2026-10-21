package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    private GraphInfoService service;
    @Autowired
    private ApartmentFacilityService facilityService;
    @Autowired
    private ApartmentLabelService labelService;
    @Autowired
    private ApartmentFeeValueService feeValueService;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private FeeValueMapper feeValueMapper;
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isUpadte=apartmentSubmitVo.getId()!=null;
        super.saveOrUpdate(apartmentSubmitVo);
        if(isUpadte){
            //删除图片
            LambdaUpdateWrapper<GraphInfo> graphInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            graphInfoLambdaUpdateWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoLambdaUpdateWrapper.eq(GraphInfo::getId, apartmentSubmitVo.getId());
            service.remove(graphInfoLambdaUpdateWrapper);
            //删除公寓信息列表
            LambdaUpdateWrapper<ApartmentFacility> apartmentFacilityUpdateWrapper = new LambdaUpdateWrapper<>();
            apartmentFacilityUpdateWrapper.eq(ApartmentFacility::getId, apartmentSubmitVo.getId());
            facilityService.remove(apartmentFacilityUpdateWrapper);
            //删除标签列表
            LambdaUpdateWrapper<ApartmentLabel> apartmentLabelUpdateWrapper = new LambdaUpdateWrapper<>();
            apartmentLabelUpdateWrapper.eq(ApartmentLabel::getId, apartmentSubmitVo.getId());
            labelService.remove(apartmentLabelUpdateWrapper);
            //删除杂费列表
            LambdaUpdateWrapper<ApartmentFeeValue> apartmentFeeValueLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            apartmentFeeValueLambdaUpdateWrapper.eq(ApartmentFeeValue::getId, apartmentSubmitVo.getId());
            feeValueService.remove(apartmentFeeValueLambdaUpdateWrapper);
        }
        //插入图片列表
        List<GraphVo> graphVoList=apartmentSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfos = new ArrayList<>();
            for(GraphVo graphVo:graphVoList){
                GraphInfo graphInfo=new GraphInfo();
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfos.add(graphInfo);
            }
            service.saveBatch(graphInfos);
        }

        //插入配套列表
        List<Long> facilityInfoIds = apartmentSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIds)){
            ArrayList<ApartmentFacility> apartmentFacilities = new ArrayList<>();
            for(Long facilityInfoId:facilityInfoIds){
                ApartmentFacility apartmentFacility = ApartmentFacility.builder().build();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(facilityInfoId);
                apartmentFacilities.add(apartmentFacility);

            }
            facilityService.saveBatch(apartmentFacilities);
        }
        //插入标签列表
        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        if (!CollectionUtils.isEmpty(labelIds)){
            ArrayList<ApartmentLabel> apartmentLabels = new ArrayList<>();
            for(Long labelId:labelIds){
                ApartmentLabel apartmentLabel = ApartmentLabel.builder().build();
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                apartmentLabel.setLabelId(labelId);
                apartmentLabels.add(apartmentLabel);
            }
            labelService.saveBatch(apartmentLabels);
        }

        //插入杂费列表
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if (!CollectionUtils.isEmpty(feeValueIds)){
            ArrayList<ApartmentFeeValue> apartmentFeeValues = new ArrayList<>();
            for(Long feeValiue:feeValueIds){
                ApartmentFeeValue apartmentFeeValue = ApartmentFeeValue.builder().build();
                apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                apartmentFeeValue.setFeeValueId(feeValiue);
                apartmentFeeValues.add(apartmentFeeValue);
            }
            feeValueService.saveBatch(apartmentFeeValues);
        }

    }

    @Override
    public IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo) {

        return apartmentInfoMapper.pageItem(page,queryVo);
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //查询公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        //查询图片列表
      List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypAndId(ItemType.APARTMENT,id);
        //查询标签列表
       List<LabelInfo> labelInfolist =labelInfoMapper.selectListByItemTypAndId(id);
        //查询配套列表
        List<FacilityInfo> feeValueIds =facilityInfoMapper.selectListByItemTypAndId(id);
        //杂费值列表
        List<FeeValueVo> feeValueVoList= feeValueMapper.selectListByItemTypAndId(id);
        //组装
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfolist);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        apartmentDetailVo.setFacilityInfoList(feeValueIds);
        return apartmentDetailVo;
    }
    //根据id删除公寓信息
    @Override
    public void removeApartmentById(Long id) {
        LambdaUpdateWrapper<RoomInfo> roomInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        roomInfoLambdaUpdateWrapper.eq(RoomInfo::getApartmentId, id);
        Long count = roomInfoMapper.selectCount(roomInfoLambdaUpdateWrapper);
        if (count>0){
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }
        super.removeById(id);
        //删除图片
        LambdaUpdateWrapper<GraphInfo> graphInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        graphInfoLambdaUpdateWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphInfoLambdaUpdateWrapper.eq(GraphInfo::getId, id);
        service.remove(graphInfoLambdaUpdateWrapper);
        //删除公寓信息列表
        LambdaUpdateWrapper<ApartmentFacility> apartmentFacilityUpdateWrapper = new LambdaUpdateWrapper<>();
        apartmentFacilityUpdateWrapper.eq(ApartmentFacility::getId,id);
        facilityService.remove(apartmentFacilityUpdateWrapper);
        //删除标签列表
        LambdaUpdateWrapper<ApartmentLabel> apartmentLabelUpdateWrapper = new LambdaUpdateWrapper<>();
        apartmentLabelUpdateWrapper.eq(ApartmentLabel::getId, id);
        labelService.remove(apartmentLabelUpdateWrapper);
        //删除杂费列表
        LambdaUpdateWrapper<ApartmentFeeValue> apartmentFeeValueLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        apartmentFeeValueLambdaUpdateWrapper.eq(ApartmentFeeValue::getId, id);
        feeValueService.remove(apartmentFeeValueLambdaUpdateWrapper);
    }
}




