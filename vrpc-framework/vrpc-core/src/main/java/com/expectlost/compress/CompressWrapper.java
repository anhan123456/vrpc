package com.expectlost.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompressWrapper {
    private byte code;
    private String type;
    private Compressor compressor;
}
