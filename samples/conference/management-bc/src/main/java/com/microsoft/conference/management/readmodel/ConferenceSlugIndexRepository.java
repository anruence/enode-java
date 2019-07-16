package com.microsoft.conference.management.readmodel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microsoft.conference.common.dataobject.ConferenceSlugIndexDO;
import com.microsoft.conference.common.mapper.ConferenceSlugIndexMapper;
import com.microsoft.conference.management.domain.models.ConferenceSlugIndex;
import com.microsoft.conference.management.domain.repositories.IConferenceSlugIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConferenceSlugIndexRepository implements IConferenceSlugIndexRepository {

    @Autowired
    private ConferenceSlugIndexMapper conferenceSlugIndexMapper;

    @Override
    public void add(ConferenceSlugIndex index) {
        ConferenceSlugIndexDO slugIndexDO = new ConferenceSlugIndexDO();
        slugIndexDO.setConferenceId(index.getConferenceId());
        slugIndexDO.setIndexId(index.getIndexId());
        slugIndexDO.setSlug(index.getSlug());
        conferenceSlugIndexMapper.insert(slugIndexDO);
    }

    @Override
    public ConferenceSlugIndex findSlugIndex(String slug) {
        LambdaQueryWrapper<ConferenceSlugIndexDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConferenceSlugIndexDO::getSlug, slug);
        ConferenceSlugIndexDO slugIndexDO = conferenceSlugIndexMapper.selectOne(queryWrapper);
        return ConferenceConvert.INSTANCE.toDTO(slugIndexDO);
    }
}
