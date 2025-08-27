package forfun.williamcolton.c.inclove.exception;

import java.io.Serializable;

public interface ErrorCode extends Serializable {

    Integer getCode();

    String getMessage();

}
