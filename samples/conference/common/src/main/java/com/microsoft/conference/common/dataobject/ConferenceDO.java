package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@TableName(value = "conference")
@Getter
@Setter
public class ConferenceDO {
    private Long id;
    private String conferenceId;
    private String accessCode;
    private String ownerName;
    private String ownerEmail;
    private String slug;
    private String name;
    private String description;
    private String location;
    private String tagline;
    private String twitterSearch;
    private Date startDate;
    private Date endDate;
    private Byte isPublished;
    private Integer version;
    private Integer eventSequence;
}
