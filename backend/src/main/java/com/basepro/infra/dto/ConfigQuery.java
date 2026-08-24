package com.basepro.infra.dto;

import com.basepro.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigQuery extends PageQuery {

    private String name;

    private String configKey;

    private String category;

    private Integer type;

}
