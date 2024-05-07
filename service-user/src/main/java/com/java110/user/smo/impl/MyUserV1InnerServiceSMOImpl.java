/*
 * Copyright 2017-2020 吴学文 and java110 team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.java110.user.smo.impl;


import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.dto.PageDto;
import com.java110.dto.menu.MenuUserDto;
import com.java110.dto.myUser.MyUserDto;
import com.java110.intf.user.IMenuUserV1InnerServiceSMO;
import com.java110.intf.user.IMyUserInnerServiceSMO;
import com.java110.po.menuUser.MenuUserPo;
import com.java110.user.dao.IMenuUserV1ServiceDao;
import com.java110.user.dao.IMyUserServiceDao;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MyUserV1InnerServiceSMOImpl extends BaseServiceSMO implements IMyUserInnerServiceSMO {

    @Autowired
    private IMyUserServiceDao myUserServiceDaoImpl;

    @Override
    public List<MyUserDto> queryMyUsers(@RequestBody MyUserDto menuUserDto) {

        //校验是否传了 分页信息

        int page = menuUserDto.getPage();

        if (page != PageDto.DEFAULT_PAGE) {
            menuUserDto.setPage((page - 1) * menuUserDto.getRow());
        }

        List<MyUserDto> menuUsers = BeanConvertUtil.covertBeanList(myUserServiceDaoImpl.queryMyUsers(BeanConvertUtil.beanCovertMap(menuUserDto)), MyUserDto.class);

        return menuUsers;
    }


    @Override
    public int queryMyUsersCount(@RequestBody MyUserDto menuUserDto) {
        return myUserServiceDaoImpl.queryMyUsersCount(BeanConvertUtil.beanCovertMap(menuUserDto));    }

}
