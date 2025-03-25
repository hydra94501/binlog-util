package org.hydra.bean;


public enum ResultEnum {
    RESULT_OK(0, "操作成功"),
    WARNING_DATA_TRUNCATED(2, "数据过多"),
    ERROR_UNKNOWN(-1, "未知错误"),
    ERROR_FORBIDDEN(-2, "权限错误"),
    ERROR_PARAMETER(-3, "缺少参数"),
    ERROR_ILLEGAL_PARAM(-4, "参数错误"),
    ERROR_UNAUTHENTICAED(-5, "未登录"),
    ERROR_UNAUTHORIZED(-6, "未授权"),
    ERROR_BUSINESSERROR(-7, "操作失败"),
    ERROR_NO_DATA(-8, "无数据"),
    ERROR_ANALYSIS_FILE(-9, "文件解析失败"),
    ERROR_VERIFY_CODE(-10, "验证码错误"),
    ERROR_UNREGISTER(-11, "未注册"),
    PARAM_NULL(-12, "参数为空"),
    EXISTS_YES(-13, "已存在"),
    MOBILE_EXISTED(-14, "手机号已注册"),
    MOBILE_NOT_EXIST(-15, "MOBILE_NOT_EXIST"),
    OLDPASSWORDNOTFOUNT(-16, "旧密码错误"),
    PASSWORDNOTFOUNT(-17, "支付密码错误"),
    WX_NOT_BLANK(-18, "微信已经存在"),
    ERROR_COMMENT_FORBIDDEN(-19, "禁止评论"),
    ERROR_FILE_UPLOAD(-20, "文件上传失败"),
    BALANCE_NOT_ENOUGH(-21, "金额不足"),
    ERROR_ORDER_EXISTS(-22, "订单重复"),
    OPERATION_FREQUENTLY(-23, "操作频繁"),
    ERROR_AD_EXISTS(-24, "存在广告"),
    USER_NOT_EXISTS(-25, "用户不存在"),
    MOBILE_CODE_WRONG(-26, "手机验证码错误"),
    PARAM_WRONG(-27, "参数错误"),
    SIGN_WRONG(-28, "签名错误"),
    DISTRIBUTED_LOCK_WRONG(-29, "分布式锁获取失败");

    private final int code;
    private final String message;

    private ResultEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
