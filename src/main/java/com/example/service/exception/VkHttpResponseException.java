package com.example.service.exception;

/**
 * Description: плохой ответ
 * Creation date: 11.07.2016 11:53
 *
 * @author sks
 */
public class VkHttpResponseException extends Exception {
    public VkHttpResponseException() {}

    public VkHttpResponseException(String message)
    {
        super(message);
    }
}
