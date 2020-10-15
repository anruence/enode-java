package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@TableName(value = "conference_slug_index")
@Getter
@Setter
public class ConferenceSlugIndexDO {
    private Long id;
    private String indexId;
    private String conferenceId;
    private String slug;
}
