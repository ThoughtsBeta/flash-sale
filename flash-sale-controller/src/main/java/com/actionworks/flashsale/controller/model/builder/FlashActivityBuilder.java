package com.actionworks.flashsale.controller.model.builder;

import com.actionworks.flashsale.app.model.command.FlashActivityPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashActivityDTO;
import com.actionworks.flashsale.controller.model.request.FlashActivityPublishRequest;
import com.actionworks.flashsale.controller.model.response.FlashActivityResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FlashActivityBuilder {
    public static FlashActivityPublishCommand toCommand(FlashActivityPublishRequest flashActivityPublishRequest) {
        if (flashActivityPublishRequest == null) {
            return null;
        }
        FlashActivityPublishCommand activityPublishCommand = new FlashActivityPublishCommand();
        BeanUtils.copyProperties(flashActivityPublishRequest, activityPublishCommand);
        return activityPublishCommand;
    }

    public static FlashActivityResponse toFlashActivityResponse(FlashActivityDTO flashActivityDTO) {
        if (flashActivityDTO == null) {
            return null;
        }
        FlashActivityResponse flashActivityResponse = new FlashActivityResponse();
        BeanUtils.copyProperties(flashActivityDTO, flashActivityResponse);
        return flashActivityResponse;
    }

    public static List<FlashActivityResponse> toFlashActivitiesResponse(Collection<FlashActivityDTO> flashActivityDTOList) {
        if (CollectionUtils.isEmpty(flashActivityDTOList)) {
            return new ArrayList<>();
        }
        return flashActivityDTOList.stream().map(FlashActivityBuilder::toFlashActivityResponse).collect(Collectors.toList());
    }
}
