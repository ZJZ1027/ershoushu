package com.basepro.system.dto;

import lombok.Data;

/**
 * 部门列表查询参数。部门数据量小且前端要自行拼树，因此不分页。
 */
@Data
public class DeptQuery {

    private String name;

    private Integer status;

}
