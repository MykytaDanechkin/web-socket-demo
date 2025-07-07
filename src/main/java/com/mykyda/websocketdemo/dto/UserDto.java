package com.mykyda.websocketdemo.dto;

import com.mykyda.websocketdemo.security.database.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
@Getter
@Setter
public class UserDto {

    Long id;

    String email;

    public static UserDto of(User user) {
        return new UserDto(user.getId(), user.getEmail());
    }
}
