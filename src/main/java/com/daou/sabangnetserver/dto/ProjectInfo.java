package com.daou.sabangnetserver.dto;

public record ProjectInfo(
        String javaVersion,
        String springBootVersion,
        String dbName,
        Long port
) {
    public ProjectInfo() {
        this("21", "3.2.2", "h2", 8099L);
    }
}
