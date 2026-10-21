package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private GraphInfoService graphInfoService;
    @Autowired
    private RoomAttrValueService roomAttrValueService;
    @Autowired
    RoomFacilityService roomFacilityService;
    @Autowired
    private RoomLabelService roomLabelService;
    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    private RoomLeaseTermService roomLeaseTermService;

    @Autowired
    private RoomInfoMapper roomInfoMapper;
    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    private GraphInfoMapper graphInfoMapper;
    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private PaymentTypeMapper paymentTypeMapper;
    @Autowired
    private LeaseTermMapper LeaseTermMapper;
    @Autowired
    private AttrValueMapper attrValueMapper;
    @Autowired
    private FacilityInfoMapper facilityInfoMapper;
    @Autowired
    private LeaseTermMapper leaseTermMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        boolean b = roomSubmitVo.getId() != null;
        super.saveOrUpdate(roomSubmitVo);

        if (b){
            //删除图片列表
            LambdaQueryWrapper<GraphInfo> graphVoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphVoLambdaQueryWrapper.eq(GraphInfo::getItemId, roomSubmitVo.getId());
            graphVoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoService.remove(graphVoLambdaQueryWrapper);
            //删除属性信息列表
            LambdaQueryWrapper<RoomAttrValue> attrKeyLambdaQueryWrapper = new LambdaQueryWrapper<>();
            attrKeyLambdaQueryWrapper.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(attrKeyLambdaQueryWrapper);
            //删除配套信息列表
            LambdaQueryWrapper<RoomFacility> roomFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomFacilityLambdaQueryWrapper.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(roomFacilityLambdaQueryWrapper);
            //删除标签信息列表
            LambdaQueryWrapper<RoomLabel> roomLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomLabelLambdaQueryWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(roomLabelLambdaQueryWrapper);
            //删除支付方式列表
            LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomPaymentTypeLambdaQueryWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(roomPaymentTypeLambdaQueryWrapper);
            //删除可选租期列表
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermLambdaQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermLambdaQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(roomLeaseTermLambdaQueryWrapper);
            //删除缓存
            String key= RedisConstant.APP_ROOM_PREFIX+roomSubmitVo.getId();
            redisTemplate.delete(key);

        }
        //添加新图片列表
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> array=new ArrayList<>();
            for (GraphVo graphVo:graphVoList){
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                array.add(graphInfo);
            }
            graphInfoService.saveBatch(array);
        }
        //添加房间属性信息列表
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(attrValueIds)){
            ArrayList<RoomAttrValue> roomAttrValueArrayList=new ArrayList<>();
            for (Long attrValueId:attrValueIds){
                RoomAttrValue roomAttrValue=RoomAttrValue.builder().roomId(roomSubmitVo.getId()).attrValueId(attrValueId).build();
                roomAttrValueArrayList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueArrayList);
        }

        //添加新房间配套信息
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIds)){
            ArrayList<RoomFacility> roomFacilityArrayList=new ArrayList<>();
            for (Long facilityInfoId:facilityInfoIds){
                RoomFacility roomFacility=RoomFacility.builder().roomId(roomSubmitVo.getId()).facilityId(facilityInfoId).build();
                roomFacilityArrayList.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilityArrayList);
        }

        //添加新标签信息列表
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if(!CollectionUtils.isEmpty(labelInfoIds)){
            ArrayList<RoomLabel> roomLabelArrayList=new ArrayList<>();
            for (Long labelInfoId:labelInfoIds){
                RoomLabel roomLabel=RoomLabel.builder().roomId(roomSubmitVo.getId()).labelId(labelInfoId).build();
                roomLabelArrayList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelArrayList);
        }

        //添加新支付方式列表
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypeIds)) {
            ArrayList<RoomPaymentType> roomPaymentTypeArrayList=new ArrayList<>();
            for (Long paymentTypeId:paymentTypeIds){
                RoomPaymentType roomPaymentType=RoomPaymentType.builder().roomId(roomSubmitVo.getId()).paymentTypeId(paymentTypeId).build();
                roomPaymentTypeArrayList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeArrayList);
        }

        //添加新可选租列表
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermIds)) {
            ArrayList<RoomLeaseTerm> roomLeaseTermArrayList=new ArrayList<>();
            for (Long leaseTermId:leaseTermIds){
                RoomLeaseTerm roomLeaseTerm=RoomLeaseTerm.builder().roomId(roomSubmitVo.getId()).leaseTermId(leaseTermId).build();
                roomLeaseTermArrayList.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTermArrayList);
        }
    }
    //根据省市区条件分页查询房间列表
    @Override
    public IPage<RoomItemVo> pageRoomItemByQuery(IPage<RoomItemVo> page, RoomQueryVo queryVo) {

        return roomInfoMapper.pageRoomItemByQuery(page,queryVo);
    }

    @Override
    public RoomDetailVo getRoomDetailById(Long id) {

        //1.查询RoomInfo
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if (roomInfo == null) {
            throw new RuntimeException("房间不存在，id=" + id);  // 或自定义业务异常
        }
        //2.查询所属公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());

        //3.查询graphInfoList
        List<GraphVo> graphVoList = graphInfoMapper.selectListByRoomId(ItemType.ROOM, id);

        //4.查询attrValueList
        List<AttrValueVo> attrvalueVoList = attrValueMapper.selectListByRoomId(id);

        //5.查询facilityInfoList
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);

        //6.查询labelInfoList
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

        //7.查询paymentTypeList
        List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

        //8.查询leaseTermList
        List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);


        RoomDetailVo adminRoomDetailVo = new RoomDetailVo();
        BeanUtils.copyProperties(roomInfo, adminRoomDetailVo);

       adminRoomDetailVo.setApartmentInfo(apartmentInfo);
        adminRoomDetailVo.setGraphVoList(graphVoList);
        adminRoomDetailVo.setAttrValueVoList(attrvalueVoList);
        adminRoomDetailVo.setFacilityInfoList(facilityInfoList);
        adminRoomDetailVo.setLabelInfoList(labelInfoList);
        adminRoomDetailVo.setPaymentTypeList(paymentTypeList);
        adminRoomDetailVo.setLeaseTermList(leaseTermList);

        return adminRoomDetailVo;
    }

    @Override
    public void removeRoomById(Long id) {
        super.removeById(id);
        //删除图片列表
        LambdaQueryWrapper<GraphInfo> graphVoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphVoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        graphVoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(graphVoLambdaQueryWrapper);
        //删除房间属性
        LambdaQueryWrapper<RoomAttrValue> attrValueVoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        attrValueVoLambdaQueryWrapper.eq(RoomAttrValue::getRoomId,id);
        roomAttrValueService.remove(attrValueVoLambdaQueryWrapper);
        //删除房间配套列表
        LambdaQueryWrapper<RoomFacility> facilityInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        facilityInfoLambdaQueryWrapper.eq(RoomFacility::getRoomId,id);
        roomFacilityService.remove(facilityInfoLambdaQueryWrapper);
        //删除标签列表
        LambdaUpdateWrapper<RoomLabel> labelInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        labelInfoLambdaUpdateWrapper.eq(RoomLabel::getRoomId,id);
        roomLabelService.remove(labelInfoLambdaUpdateWrapper);
        //删除支付方式列表
        LambdaUpdateWrapper<RoomPaymentType> roomPaymentTypeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        roomPaymentTypeLambdaUpdateWrapper.eq(RoomPaymentType::getRoomId,id);
        roomPaymentTypeService.remove(roomPaymentTypeLambdaUpdateWrapper);
        //删除可选租状态
        LambdaUpdateWrapper<RoomLeaseTerm> roomLeaseTermLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        roomLeaseTermLambdaUpdateWrapper.eq(RoomLeaseTerm::getRoomId,id);
        roomLeaseTermService.remove(roomLeaseTermLambdaUpdateWrapper);
        //删除缓存
        String key=RedisConstant.APP_ROOM_PREFIX+id;
        redisTemplate.delete(key);

    }
}




