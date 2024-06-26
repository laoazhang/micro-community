package com.java110.intf.user;

import com.java110.config.feign.FeignConfiguration;
import com.java110.dto.activities.ActivitiesBeautifulStaffDto;
import com.java110.po.activitiesBeautifulStaff.ActivitiesBeautifulStaffPo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @ClassName IActivitiesBeautifulStaffInnerServiceSMO
 * @Description 活动规则接口类
 * @Author wuxw
 * @Date 2019/4/24 9:04
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
@FeignClient(name = "user-service", configuration = {FeignConfiguration.class})
@RequestMapping("/activitiesBeautifulStaffApi")
public interface IActivitiesBeautifulStaffInnerServiceSMO {


    @RequestMapping(value = "/saveActivitiesBeautifulStaff", method = RequestMethod.POST)
    public int saveActivitiesBeautifulStaff(@RequestBody ActivitiesBeautifulStaffPo activitiesBeautifulStaffPo);

    @RequestMapping(value = "/updateActivitiesBeautifulStaff", method = RequestMethod.POST)
    public int updateActivitiesBeautifulStaff(@RequestBody  ActivitiesBeautifulStaffPo activitiesBeautifulStaffPo);

    @RequestMapping(value = "/deleteActivitiesBeautifulStaff", method = RequestMethod.POST)
    public int deleteActivitiesBeautifulStaff(@RequestBody  ActivitiesBeautifulStaffPo activitiesBeautifulStaffPo);

    /**
     * <p>查询小区楼信息</p>
     *
     *
     * @param activitiesBeautifulStaffDto 数据对象分享
     * @return ActivitiesBeautifulStaffDto 对象数据
     */
    @RequestMapping(value = "/queryActivitiesBeautifulStaffs", method = RequestMethod.POST)
    List<ActivitiesBeautifulStaffDto> queryActivitiesBeautifulStaffs(@RequestBody ActivitiesBeautifulStaffDto activitiesBeautifulStaffDto);

    /**
     * 查询<p>小区楼</p>总记录数
     *
     * @param activitiesBeautifulStaffDto 数据对象分享
     * @return 小区下的小区楼记录数
     */
    @RequestMapping(value = "/queryActivitiesBeautifulStaffsCount", method = RequestMethod.POST)
    int queryActivitiesBeautifulStaffsCount(@RequestBody ActivitiesBeautifulStaffDto activitiesBeautifulStaffDto);
}
