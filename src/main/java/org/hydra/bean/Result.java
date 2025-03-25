package org.hydra.bean;

import lombok.Data;
import lombok.ToString;
import org.slf4j.MDC;

@ToString
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private String traceId;

    public Result(ResultEnum resultEnum, T data) {
        this.traceId = MDC.get("traceId");
        this.code = resultEnum.getCode();
        this.message = resultEnum.getMessage();
        this.data = data;
    }

    public Result(ResultEnum resultEnum, String message) {
        this.traceId = MDC.get("traceId");
        this.code = resultEnum.getCode();
        this.message = message;
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(ResultEnum.RESULT_OK, data);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<T>(ResultEnum.ERROR_BUSINESSERROR, message);
    }

}
