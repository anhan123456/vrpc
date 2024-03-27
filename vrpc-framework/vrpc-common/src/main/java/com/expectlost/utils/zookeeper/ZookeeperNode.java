package com.expectlost.utils.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ZookeeperNode {
    private String nodePath;
    private byte[] data;
}
