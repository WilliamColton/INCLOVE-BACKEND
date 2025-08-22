package forfun.williamcolton.c.inclove.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserPreference {

    private Long id;
    private String userId;
    private Integer preferredAgeMin;
    private Integer preferredAgeMax;

    @TableField(value = "accepted_genders", typeHandler = JacksonTypeHandler.class)
    private Set<String> acceptedGenders;

    @TableField(value = "preferred_hobbies", typeHandler = JacksonTypeHandler.class)
    private Set<String> preferredHobbies;

    @TableField(value = "preferred_traits", typeHandler = JacksonTypeHandler.class)
    private Set<String> preferredTraits;

    private LocalDateTime updatedAt;

}
