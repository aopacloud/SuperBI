package net.aopacloud.superbi.common.core.exception;

/**
 * ObjectNotFoundException
 *
 * @author: hudong
 * @date: 2023/8/4
 * @description:
 */
public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException() {
        super();
    }
}
