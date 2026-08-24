package com.basepro.system.dto;

import java.util.List;
import java.util.Map;

/**
 * 用户导入结果：逐行处理，单行失败不影响其它行。
 *
 * @param createUsernames  新增成功的账号
 * @param updateUsernames  更新成功的账号
 * @param failureUsernames 失败的账号 -> 失败原因
 */
public record UserImportResult(List<String> createUsernames,
                               List<String> updateUsernames,
                               Map<String, String> failureUsernames) {
}
