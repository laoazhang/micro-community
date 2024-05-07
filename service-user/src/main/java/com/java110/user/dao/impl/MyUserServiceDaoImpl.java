package com.java110.user.dao.impl;

import com.java110.core.base.dao.BaseServiceDao;
import com.java110.dto.PageDto;
import com.java110.dto.myUser.MyUserDto;
import com.java110.user.dao.IMenuUserV1ServiceDao;
import com.java110.user.dao.IMyUserServiceDao;
import com.java110.utils.exception.DAOException;
import com.java110.utils.util.BeanConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * [一句话描述该类的功能]
 *
 * @author : [zhihe]
 * @version : [v1.0]
 * @createTime : [2023/12/14 10:29]
 */
@Service("myUserServiceDaoImpl")
public class MyUserServiceDaoImpl extends BaseServiceDao implements IMyUserServiceDao {
    private static Logger logger = LoggerFactory.getLogger(MenuUserV1ServiceDaoImpl.class);

    @Override
    public List<Map> queryMyUsers(Map info) throws DAOException {
        logger.debug("查询 getMenuUserInfo 入参 info : {}",info);

        List<Map> businessMenuUserInfos = sqlSessionTemplate.selectList("myUserServiceDaoImpl.queryMyUser",info);

        return businessMenuUserInfos;
    }

    @Override
    public int queryMyUsersCount(Map info) throws DAOException {
        logger.debug("查询 queryMenuUsersCount 入参 info : {}",info);

        List<Map> businessMenuUserInfos = sqlSessionTemplate.selectList("myUserServiceDaoImpl.queryMyUsersCount", info);
        if (businessMenuUserInfos.size() < 1) {
            return 0;
        }

        return Integer.parseInt(businessMenuUserInfos.get(0).get("count").toString());
    }
}
