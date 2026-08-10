package com.panduoma.trevaljava.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor//全参注解
public class StreamChunkVO {
    private String type="chunk";
    private String content;
    public static StreamChunkVO of(String content){
        return new StreamChunkVO("chunk",content);
    }

}
