package com.tlg.storehelper.vo;

import java.io.Serializable;

public class MembershipVo implements Serializable {
    /**卡号*/
    public String membershipCardId;
    /**姓名*/
    public String membershipName;
    /**手机号*/
    public String mobile;
    /**本年消费额*/
    public int yearExpenditure;
    /**总消费额*/
    public int totalExpenditure;

}
