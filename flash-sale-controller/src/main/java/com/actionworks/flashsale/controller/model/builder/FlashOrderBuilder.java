package com.actionworks.flashsale.controller.model.builder;

import com.actionworks.flashsale.app.model.command.FlashPlaceOrderCommand;
import com.actionworks.flashsale.app.model.dto.FlashOrderDTO;
import com.actionworks.flashsale.controller.model.request.FlashPlaceOrderRequest;
import com.actionworks.flashsale.controller.model.response.FlashOrderResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FlashOrderBuilder {
    public static FlashPlaceOrderCommand toCommand(FlashPlaceOrderRequest flashPlaceOrderRequest) {
        FlashPlaceOrderCommand flashPlaceOrderCommand = new FlashPlaceOrderCommand();
        BeanUtils.copyProperties(flashPlaceOrderRequest, flashPlaceOrderCommand);
        return flashPlaceOrderCommand;
    }

    public static FlashOrderResponse toFlashOrderResponse(FlashOrderDTO flashOrderDTO) {
        if (flashOrderDTO == null) {
            return null;
        }
        FlashOrderResponse flashOrderResponse = new FlashOrderResponse();
        BeanUtils.copyProperties(flashOrderDTO, flashOrderResponse);
        return flashOrderResponse;
    }

    public static List<FlashOrderResponse> toFlashOrdersResponse(Collection<FlashOrderDTO> flashOrderDTOList) {
        if (CollectionUtils.isEmpty(flashOrderDTOList)) {
            return new ArrayList<>();
        }
        return flashOrderDTOList.stream().map(FlashOrderBuilder::toFlashOrderResponse).collect(Collectors.toList());
    }
}
