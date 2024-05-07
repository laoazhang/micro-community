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
package com.java110.intf.user;

import com.java110.config.feign.FeignConfiguration;
import com.java110.dto.menu.MenuUserDto;
import com.java110.dto.myUser.MyUserDto;
import com.java110.po.menuUser.MenuUserPo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


@FeignClient(name = "user-service", configuration = {FeignConfiguration.class})
@RequestMapping("/myUserV1Api")
public interface IMyUserInnerServiceSMO {


    @RequestMapping(value = "/queryMyUsers", method = RequestMethod.POST)
    List<MyUserDto> queryMyUsers(@RequestBody MyUserDto myUserDto);


    @RequestMapping(value = "/queryMyUsersCount", method = RequestMethod.POST)
    int queryMyUsersCount(@RequestBody MyUserDto myUserDto);
}
