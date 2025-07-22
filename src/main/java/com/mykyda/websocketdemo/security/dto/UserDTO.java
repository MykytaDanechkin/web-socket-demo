package com.mykyda.websocketdemo.security.dto;

import com.mykyda.websocketdemo.security.database.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Value
@FieldNameConstants
@Getter
@Setter
public class UserDTO {

    Long id;

    String email;

    public static UserDTO of(User user) {
        return new UserDTO(user.getId(), user.getEmail());
    }
}
