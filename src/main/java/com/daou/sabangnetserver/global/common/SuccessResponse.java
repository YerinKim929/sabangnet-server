package com.daou.sabangnetserver.global.common;

import lombok.Builder;

@Builder
public record SuccessResponse(int code, String message, Object data) {

}
