package com.java110.community.bmo.assets.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.community.bmo.assets.IQueryAssetsRepairBMO;
import com.java110.intf.common.IMachineInnerServiceSMO;
import com.java110.intf.community.IParkingSpaceInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class QueryAssetsRepairBMOImpl implements IQueryAssetsRepairBMO {

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IParkingSpaceInnerServiceSMO parkingSpaceInnerServiceSMOImpl;

    @Autowired
    private IMachineInnerServiceSMO machineInnerServiceSMOImpl;

    /**
     * @param communityId
     * @return {
     * data:{
     * floorCount:30,
     * roomCount:29,
     * parkingSpaceCount:12,
     * machineCount:12
     * }
     * }
     */
    @Override
    public ResponseEntity<String> query(String communityId) {
        JSONObject data = new JSONObject();

        return ResultVo.createResponseEntity(data);
    }
}
