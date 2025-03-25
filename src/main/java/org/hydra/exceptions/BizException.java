package org.hydra.exceptions;

public class BizException extends RuntimeException {
    private static final long serialVersionUID = 6806129545290130132L;

    public BizException(String message) {
        super(message) ;
    }

    public static void check(Boolean condition  , String message ) {
        if (condition) {
            throw (new BizException(message));
        }
    }

}