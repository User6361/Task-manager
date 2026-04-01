    package com.main.taskmanager.user.web;

    import com.main.taskmanager.user.model.enumclasses.Role;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import tools.jackson.databind.PropertyNamingStrategies;
    import tools.jackson.databind.annotation.JsonNaming;

    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private Set<Role> roles = new HashSet<>();
    }
