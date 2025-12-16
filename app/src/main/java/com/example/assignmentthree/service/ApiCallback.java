package com.example.assignmentthree.service;

/**
 * 通用 API 异步回调接口，用于处理 Volley 请求结果
 * @param <T> 期望返回的数据类型
 */
public interface ApiCallback<T> {
    void onSuccess(T result);
    void onError(String error);
}
