package com.example.service.exception;

/**
 * Description: ошибка полученных данных
 * Creation date: 11.07.2016 18:19
 *
 * @author sks
 */
public class VkDataException extends  Exception{
    public VkDataException() {}

    public VkDataException(String message)
    {
        super(message);
    }
}
