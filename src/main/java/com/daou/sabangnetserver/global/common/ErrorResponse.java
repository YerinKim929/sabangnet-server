package com.daou.sabangnetserver.global.common;

import lombok.Builder;

@Builder
public record ErrorResponse(int code, String message) {

}
