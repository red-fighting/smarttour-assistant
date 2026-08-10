package com.panduoma.trevaljava.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

@Data
public class ChatRequestDTO {
    @NotBlank(message = "消息不能为空")
    private String message;
}
