package com.actionworks.flashsale.controller.model.builder;

import com.actionworks.flashsale.app.model.command.FlashItemPublishCommand;
import com.actionworks.flashsale.app.model.dto.FlashItemDTO;
import com.actionworks.flashsale.controller.model.request.FlashItemPublishRequest;
import com.actionworks.flashsale.controller.model.response.FlashItemResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FlashItemBuilder {
    public static FlashItemPublishCommand toCommand(FlashItemPublishRequest flashItemPublishRequest) {
        if (flashItemPublishRequest == null) {
            return null;
        }
        FlashItemPublishCommand flashItemPublishCommand = new FlashItemPublishCommand();
        BeanUtils.copyProperties(flashItemPublishRequest, flashItemPublishCommand);
        return flashItemPublishCommand;
    }

    public static FlashItemResponse toFlashItemResponse(FlashItemDTO flashItemDTO) {
        if (flashItemDTO == null) {
            return null;
        }
        FlashItemResponse flashItemResponse = new FlashItemResponse();
        BeanUtils.copyProperties(flashItemDTO, flashItemResponse);
        return flashItemResponse;
    }

    public static List<FlashItemResponse> toFlashItemsResponse(Collection<FlashItemDTO> flashItemDTOList) {
        if (CollectionUtils.isEmpty(flashItemDTOList)) {
            return new ArrayList<>();
        }
        return flashItemDTOList.stream().map(FlashItemBuilder::toFlashItemResponse).collect(Collectors.toList());
    }
}
