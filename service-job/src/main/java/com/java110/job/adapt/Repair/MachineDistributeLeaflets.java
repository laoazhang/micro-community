package com.java110.job.adapt.Repair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.factory.CommunitySettingFactory;
import com.java110.core.factory.WechatFactory;
import com.java110.core.log.LoggerFactory;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.file.FileRelDto;
import com.java110.dto.machine.MachinePrinterDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.printerRule.PrinterRuleDto;
import com.java110.dto.printerRule.PrinterRuleMachineDto;
import com.java110.dto.printerRule.PrinterRuleRepairDto;
import com.java110.dto.smallWeChat.SmallWeChatDto;
import com.java110.dto.smallWechatAttr.SmallWechatAttrDto;
import com.java110.dto.staffAppAuth.StaffAppAuthDto;
import com.java110.dto.user.UserDto;
import com.java110.entity.order.Business;
import com.java110.entity.wechat.Content;
import com.java110.entity.wechat.Data;
import com.java110.entity.wechat.PropertyFeeTemplateMessage;
import com.java110.intf.common.*;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.intf.store.ISmallWeChatInnerServiceSMO;
import com.java110.intf.store.ISmallWechatAttrInnerServiceSMO;
import com.java110.intf.user.IOwnerAppUserInnerServiceSMO;
import com.java110.intf.user.IStaffAppAuthInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.job.printer.IPrinter;
import com.java110.po.owner.RepairUserPo;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.cache.UrlCache;
import com.java110.utils.constant.MappingConstant;
import com.java110.utils.exception.CmdException;
import com.java110.utils.factory.ApplicationContextFactory;
import com.java110.utils.util.Assert;
import com.java110.utils.util.ImageUtils;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 派单(抢单、转单)通知适配器
 *
 * @author fqz
 * @date 2021-01-13 13:55
 */
@Component(value = "machineDistributeLeaflets")
public class MachineDistributeLeaflets extends DatabusAdaptImpl {

    private static Logger logger = LoggerFactory.getLogger(MachineDistributeLeaflets.class);

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMO;

    @Autowired
    private ISmallWeChatInnerServiceSMO smallWeChatInnerServiceSMOImpl;

    @Autowired
    private ISmallWechatAttrInnerServiceSMO smallWechatAttrInnerServiceSMOImpl;

    @Autowired
    private IStaffAppAuthInnerServiceSMO staffAppAuthInnerServiceSMO;

    @Autowired
    private RestTemplate outRestTemplate;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMO;

    @Autowired
    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMO;

    @Autowired
    private IFileRelInnerServiceSMO fileRelInnerServiceSMOImpl;


    @Autowired
    private IPrinterRuleRepairV1InnerServiceSMO printerRuleRepairV1InnerServiceSMOImpl;

    @Autowired
    private IPrinterRuleV1InnerServiceSMO printerRuleV1InnerServiceSMOImpl;

    @Autowired
    private IPrinterRuleMachineV1InnerServiceSMO printerRuleMachineV1InnerServiceSMOImpl;

    @Autowired
    private IMachinePrinterV1InnerServiceSMO machinePrinterV1InnerServiceSMOImpl;

    //模板信息推送地址
    private static String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";

    @Override
    public void execute(Business business, List<Business> businesses) {
        JSONObject data = business.getData();
        JSONArray businessRepairUsers = new JSONArray();
        System.out.println("收到日志：>>>>>>>>>>>>>" + data.toJSONString());
        if (data.containsKey(RepairUserPo.class.getSimpleName())) {
            Object bObj = data.get(RepairUserPo.class.getSimpleName());
            if (bObj instanceof JSONObject) {
                businessRepairUsers.add(bObj);
            } else if (bObj instanceof List) {
                businessRepairUsers = JSONArray.parseArray(JSONObject.toJSONString(bObj));
            } else {
                businessRepairUsers = (JSONArray) bObj;
            }
        } else {
            if (data instanceof JSONObject) {
                businessRepairUsers.add(data);
            }
        }
        for (int bOwnerRepairIndex = 0; bOwnerRepairIndex < businessRepairUsers.size(); bOwnerRepairIndex++) {
            JSONObject businessRepairUser = businessRepairUsers.getJSONObject(bOwnerRepairIndex);
            doDealOwnerRepair(businesses, businessRepairUser);
        }
    }

    private void doDealOwnerRepair(List<Business> businesses, JSONObject businessRepairUser) {

    }

    /**
     * // 自动打印小票
     * @param ruId
     * @param repairType
     * @param communityDto
     */
    private void autoPrintRepair(String ruId,String repairType, CommunityDto communityDto) {

        PrinterRuleRepairDto printerRuleRepairDto = new PrinterRuleRepairDto();
        printerRuleRepairDto.setCommunityId(communityDto.getCommunityId());
        printerRuleRepairDto.setRepairType(repairType);
        List<PrinterRuleRepairDto> printerRuleRepairDtos = printerRuleRepairV1InnerServiceSMOImpl.queryPrinterRuleRepairs(printerRuleRepairDto);

        if (printerRuleRepairDtos == null || printerRuleRepairDtos.size() < 1) {
            return;
        }

        PrinterRuleDto printerRuleDto = new PrinterRuleDto();
        printerRuleDto.setRuleId(printerRuleRepairDtos.get(0).getRuleId());
        printerRuleDto.setCommunityId(communityDto.getCommunityId());
        printerRuleDto.setState(PrinterRuleDto.STATE_NORMAL);
        int count = printerRuleV1InnerServiceSMOImpl.queryPrinterRulesCount(printerRuleDto);

        if (count < 1) {
            return;
        }

        PrinterRuleMachineDto printerRuleMachineDto = new PrinterRuleMachineDto();
        printerRuleMachineDto.setCommunityId(communityDto.getCommunityId());
        printerRuleMachineDto.setRuleId(printerRuleRepairDtos.get(0).getRuleId());
        List<PrinterRuleMachineDto> printerRuleMachineDtos = printerRuleMachineV1InnerServiceSMOImpl.queryPrinterRuleMachines(printerRuleMachineDto);
        if (printerRuleMachineDtos == null || printerRuleMachineDtos.size() < 1) {
            return;
        }

        for (PrinterRuleMachineDto tmpPrinterRuleMachineDto : printerRuleMachineDtos) {
            try {
                doPrint(tmpPrinterRuleMachineDto, ruId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private void doPrint(PrinterRuleMachineDto tmpPrinterRuleMachineDto, String ruId) {
        MachinePrinterDto machinePrinterDto = new MachinePrinterDto();
        machinePrinterDto.setCommunityId(tmpPrinterRuleMachineDto.getCommunityId());
        machinePrinterDto.setMachineId(tmpPrinterRuleMachineDto.getMachineId());
        List<MachinePrinterDto> machinePrinterDtos = machinePrinterV1InnerServiceSMOImpl.queryMachinePrinters(machinePrinterDto);

        Assert.listOnlyOne(machinePrinterDtos, "云打印机不存在");

        IPrinter printer = ApplicationContextFactory.getBean(machinePrinterDtos.get(0).getImplBean(), IPrinter.class);

        if (printer == null) {
            throw new CmdException("打印机异常，未包含适配器");
        }

        printer.printRepair(ruId, tmpPrinterRuleMachineDto.getCommunityId(), Integer.parseInt(tmpPrinterRuleMachineDto.getQuantity()), machinePrinterDtos.get(0));


    }

    /**
     * 转单给维修师傅推送信息
     *
     * @param paramIn
     * @param communityDto
     */
    private void sendMessage(JSONObject paramIn, CommunityDto communityDto) {
        //查询公众号配置
        SmallWeChatDto smallWeChatDto = new SmallWeChatDto();
        smallWeChatDto.setWeChatType("1100");
        smallWeChatDto.setObjType(SmallWeChatDto.OBJ_TYPE_COMMUNITY);
        smallWeChatDto.setObjId(communityDto.getCommunityId());
        List<SmallWeChatDto> smallWeChatDtos = smallWeChatInnerServiceSMOImpl.querySmallWeChats(smallWeChatDto);
        if (smallWeChatDto == null || smallWeChatDtos.size() <= 0) {
            logger.info("未配置微信公众号信息,定时任务执行结束");
            return;
        }
        SmallWeChatDto weChatDto = smallWeChatDtos.get(0);
        SmallWechatAttrDto smallWechatAttrDto = new SmallWechatAttrDto();
        smallWechatAttrDto.setCommunityId(communityDto.getCommunityId());
        smallWechatAttrDto.setWechatId(weChatDto.getWeChatId());
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_DISPATCH_REMIND_TEMPLATE);
        List<SmallWechatAttrDto> smallWechatAttrDtos = smallWechatAttrInnerServiceSMOImpl.querySmallWechatAttrs(smallWechatAttrDto);
        if (smallWechatAttrDtos == null || smallWechatAttrDtos.size() <= 0) {
            logger.info("未配置微信公众号消息模板");
            return;
        }
        String templateId = smallWechatAttrDtos.get(0).getValue();
        String accessToken = WechatFactory.getAccessToken(weChatDto.getAppId(), weChatDto.getAppSecret());
        if (StringUtil.isEmpty(accessToken)) {
            logger.info("推送微信模板,获取accessToken失败:{}", accessToken);
            return;
        }
        String url = sendMsgUrl + accessToken;
        //根据 userId 查询到openId
        StaffAppAuthDto staffAppAuthDto = new StaffAppAuthDto();
        staffAppAuthDto.setStaffId(paramIn.getString("staffId"));
        staffAppAuthDto.setAppType("WECHAT");
        List<StaffAppAuthDto> staffAppAuthDtos = staffAppAuthInnerServiceSMO.queryStaffAppAuths(staffAppAuthDto);
        if (staffAppAuthDtos.size() > 0) {
            String openId = staffAppAuthDtos.get(0).getOpenId();
            Data data = new Data();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            templateMessage.setTemplate_id(templateId);
            templateMessage.setTouser(openId);
            data.setFirst(new Content("您有新的维修任务，维修信息如下："));
            data.setKeyword1(new Content(paramIn.getString("repairName")));
            data.setKeyword2(new Content(paramIn.getString("tel")));
            data.setKeyword3(new Content(paramIn.getString("time")));
            data.setKeyword4(new Content(paramIn.getString("context") + "\r\n" + "报修位置：" + paramIn.getString("repairObjName")));
            data.setRemark(new Content(paramIn.getString("preStaffName") + "转单给您，请及时登录公众号接单确认！"));
            templateMessage.setData(data);
            //获取员工公众号地址
            String wechatUrl = MappingCache.getValue(MappingConstant.URL_DOMAIN,"STAFF_WECHAT_URL");
            templateMessage.setUrl(wechatUrl);
            logger.info("发送模板消息内容:{}", JSON.toJSONString(templateMessage));
            ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
            logger.info("微信模板返回内容:{}", responseEntity);
        }
    }

    /**
     * 派单给维修师傅推送信息
     *
     * @param paramIn
     * @param communityDto
     */
    private void sendMsg(JSONObject paramIn, CommunityDto communityDto) {
        //查询公众号配置
        SmallWeChatDto smallWeChatDto = new SmallWeChatDto();
        smallWeChatDto.setWeChatType("1100");
        smallWeChatDto.setObjType(SmallWeChatDto.OBJ_TYPE_COMMUNITY);
        smallWeChatDto.setObjId(communityDto.getCommunityId());
        List<SmallWeChatDto> smallWeChatDtos = smallWeChatInnerServiceSMOImpl.querySmallWeChats(smallWeChatDto);
        if (smallWeChatDto == null || smallWeChatDtos.size() <= 0) {
            logger.info("未配置微信公众号信息,定时任务执行结束");
            return;
        }
        SmallWeChatDto weChatDto = smallWeChatDtos.get(0);
        SmallWechatAttrDto smallWechatAttrDto = new SmallWechatAttrDto();
        smallWechatAttrDto.setCommunityId(communityDto.getCommunityId());
        smallWechatAttrDto.setWechatId(weChatDto.getWeChatId());
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_DISPATCH_REMIND_TEMPLATE);
        List<SmallWechatAttrDto> smallWechatAttrDtos = smallWechatAttrInnerServiceSMOImpl.querySmallWechatAttrs(smallWechatAttrDto);
        if (smallWechatAttrDtos == null || smallWechatAttrDtos.size() <= 0) {
            logger.info("未配置微信公众号消息模板");
            return;
        }
        String templateId = smallWechatAttrDtos.get(0).getValue();
        String accessToken = WechatFactory.getAccessToken(weChatDto.getAppId(), weChatDto.getAppSecret());
        if (StringUtil.isEmpty(accessToken)) {
            logger.info("推送微信模板,获取accessToken失败:{}", accessToken);
            return;
        }
        //获取具体位置
        String address = "";
        if (communityDto.getName().equals(paramIn.getString("repairObjName"))) {
            address = paramIn.getString("repairObjName");
        } else {
            address = communityDto.getName() + paramIn.getString("repairObjName");
        }
        String url = sendMsgUrl + accessToken;
        //根据 userId 查询到openId
        StaffAppAuthDto staffAppAuthDto = new StaffAppAuthDto();
        staffAppAuthDto.setStaffId(paramIn.getString("staffId"));
        staffAppAuthDto.setStaffName(paramIn.getString("staffName"));
        staffAppAuthDto.setAppType("WECHAT");
        List<StaffAppAuthDto> staffAppAuthDtos = staffAppAuthInnerServiceSMO.queryStaffAppAuths(staffAppAuthDto);
        if (staffAppAuthDtos.size() > 0) {
            String openId = staffAppAuthDtos.get(0).getOpenId();
            Data data = new Data();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            templateMessage.setTemplate_id(templateId);
            templateMessage.setTouser(openId);
            data.setFirst(new Content("您有新的维修任务，维修信息如下："));
            data.setKeyword1(new Content(paramIn.getString("repairName")));
            data.setKeyword2(new Content(paramIn.getString("tel")));
            data.setKeyword3(new Content(paramIn.getString("time")));
            data.setKeyword4(new Content(paramIn.getString("context") + "\r\n" + "报修位置：" + address));
            data.setRemark(new Content("请及时登录公众号接单确认！"));
            templateMessage.setData(data);
            //获取员工公众号地址
            String wechatUrl = MappingCache.getValue(MappingConstant.URL_DOMAIN,"STAFF_WECHAT_URL");
            templateMessage.setUrl(wechatUrl);
            logger.info("发送模板消息内容:{}", JSON.toJSONString(templateMessage));
            ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
            logger.info("微信模板返回内容:{}", responseEntity);
        }
    }


    private void sendMsgToWechatGroup(JSONObject paramIn, CommunityDto communityDto) {

        //查询公众号配置
        String url = CommunitySettingFactory.getRemark(communityDto.getCommunityId(), "WECHAT_SEND_REPAIR_URL");
        if (StringUtil.isEmpty(url)) {
            return;
        }

        JSONObject rebootParam = new JSONObject();
        rebootParam.put("msgtype", "markdown");
        JSONObject rebootMarkdown = new JSONObject();
        rebootParam.put("markdown", rebootMarkdown);


        //获取具体位置
        String address = "";
        if (communityDto.getName().equals(paramIn.getString("repairObjName"))) {
            address = paramIn.getString("repairObjName");
        } else {
            address = communityDto.getName() + paramIn.getString("repairObjName");
        }

        //根据 userId 查询到openId
        UserDto userDto = new UserDto();
        userDto.setUserId(paramIn.getString("staffId"));
        List<UserDto> userDtos = userInnerServiceSMO.getUsers(userDto);
        String staffName = "";
        if (userDtos != null && userDtos.size() > 0) {
            staffName = userDtos.get(0).getName();
        }
        String content = staffName + " 您有新的维修任务，维修信息如下：\n";

        content += ("> 标题：<font color=\"comment\">" + paramIn.getString("repairName") + "</font> \n");
        content += ("> 电话：<font color=\"comment\">" + paramIn.getString("tel") + "</font> \n");
        content += ("> 时间：<font color=\"comment\">" + paramIn.getString("time") + "</font> \n");
        content += ("> 内容：<font color=\"comment\">" + paramIn.getString("context") + "</font> \n");
        content += ("> 位置：<font color=\"comment\">" + address + "</font> \n");
        content += ("> 单号：<font color=\"comment\">" + paramIn.getString("repairId") + "</font> \n");

        rebootMarkdown.put("content", content);
        logger.info("发送消息内容:{}", content);
        ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, rebootParam.toJSONString(), String.class);
        logger.info("企业微信返回内容:{}", responseEntity);


        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return;
        }

        String imgUrl = MappingCache.getValue(MappingConstant.FILE_DOMAIN,"IMG_PATH");
        FileRelDto fileRelDto = new FileRelDto();
        fileRelDto.setObjId(paramIn.getString("repairId"));
        List<FileRelDto> fileRelDtos = fileRelInnerServiceSMOImpl.queryFileRels(fileRelDto);

        if (fileRelDtos == null || fileRelDtos.size() < 1) {
            return;
        }

        rebootParam = JSONObject.parseObject(" {\n" +
                "            \"msgtype\":\"image\",\n" +
                "            \"image\":{\n" +
                "              }\n" +
                "        }");


        JSONObject image = rebootParam.getJSONObject("image");

        String imageUrl = "";
        for (FileRelDto tmpFileRelDto : fileRelDtos) {

            if (!tmpFileRelDto.getRelTypeCd().equals(FileRelDto.REL_TYPE_CD_REPAIR)) {  //维修图片
                continue;
            }
            imageUrl = imgUrl + tmpFileRelDto.getFileRealName();
            image.put("base64", ImageUtils.getBase64ByImgUrl(imageUrl));
            image.put("md5", ImageUtils.getMd5ByImgUrl(imageUrl));
            responseEntity = outRestTemplate.postForEntity(url, rebootParam.toJSONString(), String.class);
            logger.debug("返回信息：" + responseEntity);
        }

    }


    /**
     * 派单(抢单)成功后给业主推送信息
     *
     * @param paramIn
     * @param communityDto
     */
    private void publishMsg(JSONObject paramIn, CommunityDto communityDto) {
        //查询公众号配置
        SmallWeChatDto smallWeChatDto = new SmallWeChatDto();
        smallWeChatDto.setWeChatType("1100");
        smallWeChatDto.setObjType(SmallWeChatDto.OBJ_TYPE_COMMUNITY);
        smallWeChatDto.setObjId(communityDto.getCommunityId());
        List<SmallWeChatDto> smallWeChatDtos = smallWeChatInnerServiceSMOImpl.querySmallWeChats(smallWeChatDto);
        if (smallWeChatDto == null || smallWeChatDtos.size() <= 0) {
            logger.info("未配置微信公众号信息,定时任务执行结束");
            return;
        }
        SmallWeChatDto weChatDto = smallWeChatDtos.get(0);
        SmallWechatAttrDto smallWechatAttrDto = new SmallWechatAttrDto();
        smallWechatAttrDto.setCommunityId(communityDto.getCommunityId());
        smallWechatAttrDto.setWechatId(weChatDto.getWeChatId());
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_SCHEDULE_TEMPLATE);
        List<SmallWechatAttrDto> smallWechatAttrDtos = smallWechatAttrInnerServiceSMOImpl.querySmallWechatAttrs(smallWechatAttrDto);
        if (smallWechatAttrDtos == null || smallWechatAttrDtos.size() <= 0) {
            logger.info("未配置微信公众号消息模板");
            return;
        }
        String templateId = smallWechatAttrDtos.get(0).getValue();
        String accessToken = WechatFactory.getAccessToken(weChatDto.getAppId(), weChatDto.getAppSecret());
        if (StringUtil.isEmpty(accessToken)) {
            logger.info("推送微信模板,获取accessToken失败:{}", accessToken);
            return;
        }
        //查询维修员工信息
        UserDto userDto = new UserDto();
        userDto.setUserId(paramIn.getString("staffId"));
        userDto.setStatusCd("0");
        List<UserDto> users = userInnerServiceSMO.getUsers(userDto);
        //获取维修员工姓名
        String name = users.get(0).getName();
        //获取维修员工联系方式
        String tel = users.get(0).getTel();
        //获取用户id
        String preStaffId = paramIn.getString("preStaffId");
        if (!StringUtil.isEmpty(preStaffId)) {
            OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
            ownerAppUserDto.setUserId(preStaffId);
            ownerAppUserDto.setAppType("WECHAT");
            //查询绑定业主
            List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMO.queryOwnerAppUsers(ownerAppUserDto);
            if (ownerAppUserDtos.size() > 0) {
                //获取openId
                String openId = ownerAppUserDtos.get(0).getOpenId();
                String url = sendMsgUrl + accessToken;
                Data data = new Data();
                PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
                templateMessage.setTemplate_id(templateId);
                templateMessage.setTouser(openId);
                data.setFirst(new Content("您的报修受理成功，维修人员信息如下："));
                data.setKeyword1(new Content(paramIn.getString("context")));
                data.setKeyword2(new Content(name));
                data.setKeyword3(new Content(tel));
                data.setKeyword4(new Content(paramIn.getString("time")));
                data.setRemark(new Content("您的报修已受理，请保持电话畅通，以便维修人员及时跟您取得联系！感谢您的使用！"));
                templateMessage.setData(data);
                //获取业主公众号地址
                String wechatUrl = UrlCache.getOwnerUrl();
                if (!StringUtil.isEmpty(wechatUrl) && wechatUrl.contains("?")) {
                    wechatUrl += ("&wAppId=" + weChatDto.getAppId());
                } else {
                    wechatUrl += ("?wAppId=" + weChatDto.getAppId());
                }

                templateMessage.setUrl(wechatUrl);
                logger.info("发送模板消息内容:{}", JSON.toJSONString(templateMessage));
                ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
                logger.info("微信模板返回内容:{}", responseEntity);
            }
        }
    }

    /**
     * 抢单成功后给维修师傅推送信息
     *
     * @param paramIn
     * @param communityDto
     */
    private void publishMessage(JSONObject paramIn, CommunityDto communityDto) {
        //查询公众号配置
        SmallWeChatDto smallWeChatDto = new SmallWeChatDto();
        smallWeChatDto.setWeChatType("1100");
        smallWeChatDto.setObjType(SmallWeChatDto.OBJ_TYPE_COMMUNITY);
        smallWeChatDto.setObjId(communityDto.getCommunityId());
        List<SmallWeChatDto> smallWeChatDtos = smallWeChatInnerServiceSMOImpl.querySmallWeChats(smallWeChatDto);
        if (smallWeChatDto == null || smallWeChatDtos.size() <= 0) {
            logger.info("未配置微信公众号信息,定时任务执行结束");
            return;
        }
        SmallWeChatDto weChatDto = smallWeChatDtos.get(0);
        SmallWechatAttrDto smallWechatAttrDto = new SmallWechatAttrDto();
        smallWechatAttrDto.setCommunityId(communityDto.getCommunityId());
        smallWechatAttrDto.setWechatId(weChatDto.getWeChatId());
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_DISPATCH_REMIND_TEMPLATE);
        List<SmallWechatAttrDto> smallWechatAttrDtos = smallWechatAttrInnerServiceSMOImpl.querySmallWechatAttrs(smallWechatAttrDto);
        if (smallWechatAttrDtos == null || smallWechatAttrDtos.size() <= 0) {
            logger.info("未配置微信公众号消息模板");
            return;
        }
        String templateId = smallWechatAttrDtos.get(0).getValue();
        String accessToken = WechatFactory.getAccessToken(weChatDto.getAppId(), weChatDto.getAppSecret());
        if (StringUtil.isEmpty(accessToken)) {
            logger.info("推送微信模板,获取accessToken失败:{}", accessToken);
            return;
        }
        //获取具体位置
        String address = "";
        if (communityDto.getName().equals(paramIn.getString("repairObjName"))) {
            address = paramIn.getString("repairObjName");
        } else {
            address = communityDto.getName() + paramIn.getString("repairObjName");
        }
        //根据 userId 查询到openId
        StaffAppAuthDto staffAppAuthDto = new StaffAppAuthDto();
        staffAppAuthDto.setStaffId(paramIn.getString("staffId"));
        staffAppAuthDto.setAppType("WECHAT");
        List<StaffAppAuthDto> staffAppAuthDtos = staffAppAuthInnerServiceSMO.queryStaffAppAuths(staffAppAuthDto);

        if(staffAppAuthDtos == null || staffAppAuthDtos.size() < 1){
            logger.error("员工未做员工认证");
            return;
        }
        String openId = staffAppAuthDtos.get(0).getOpenId();
        String url = sendMsgUrl + accessToken;
        Data data = new Data();
        PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
        templateMessage.setTemplate_id(templateId);
        templateMessage.setTouser(openId);
        data.setFirst(new Content("恭喜您抢单成功，报修信息如下："));
        data.setKeyword1(new Content(paramIn.getString("repairName")));
        data.setKeyword2(new Content(paramIn.getString("tel")));
        data.setKeyword3(new Content(paramIn.getString("time")));
        data.setKeyword4(new Content(paramIn.getString("context") + "\r\n" + "报修位置：" + address));
        data.setRemark(new Content("请及时与客户取得联系！"));
        templateMessage.setData(data);
        //获取员工公众号地址
        String wechatUrl = MappingCache.getValue(MappingConstant.URL_DOMAIN,"STAFF_WECHAT_URL");
        templateMessage.setUrl(wechatUrl);
        logger.info("发送模板消息内容:{}", JSON.toJSONString(templateMessage));
        ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
        logger.info("微信模板返回内容:{}", responseEntity);
    }
}
