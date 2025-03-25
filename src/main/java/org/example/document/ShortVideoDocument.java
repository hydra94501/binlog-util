package org.example.document;

import org.apache.commons.lang3.StringUtils;
import org.example.entity.ShortVideo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShortVideoDocument extends ShortVideo {

    private List<String> labelIdList;

    public List<String> getLabelIdList(){
        if(StringUtils.isBlank(super.getLabelIds())){
            return new ArrayList<>();
        }
        return  Arrays.asList(super.getLabelIds().split(","));
    }
}
