package com.java110.dto.myUser;

import com.java110.dto.PageDto;

import java.io.Serializable;
import java.util.Date;

public class MyUserDto extends PageDto implements Serializable {

    private Long id;
    private String name;
    private String sex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
