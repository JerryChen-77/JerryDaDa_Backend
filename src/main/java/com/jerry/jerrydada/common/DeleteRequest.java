package com.jerry.jerrydada.common;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除请求
 *
 * @author <a href="https://github.com/JerryChen-77">JerryChen</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}