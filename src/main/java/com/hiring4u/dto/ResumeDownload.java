package com.hiring4u.dto;

public record ResumeDownload(byte[] data, String fileName, String contentType) {
}
